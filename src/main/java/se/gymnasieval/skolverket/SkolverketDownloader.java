package se.gymnasieval.skolverket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import se.gymnasieval.school.OrganizerType;
import se.gymnasieval.school.SchoolStatus;
import se.gymnasieval.skolverket.SkolverketClient.Address;
import se.gymnasieval.skolverket.SkolverketClient.EducationEvent;
import se.gymnasieval.skolverket.SkolverketClient.ListedSchoolUnit;
import se.gymnasieval.skolverket.SkolverketClient.SchoolUnitDetail;
import se.gymnasieval.skolverket.SkolverketSnapshot.MunicipalityData;
import se.gymnasieval.skolverket.SkolverketSnapshot.ProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.StudyPathData;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Hämtar alla gymnasieskolor från Skolverket och översätter till {@link SkolverketSnapshot}.
 * Två anrop per skola (detaljer + utbildningar) körs parallellt i virtuella trådar,
 * begränsat av {@code skolverket.concurrency} för att inte belasta API:et.
 */
@Service
public class SkolverketDownloader {

    private static final Logger log = LoggerFactory.getLogger(SkolverketDownloader.class);
    private static final int MAX_ATTEMPTS = 3;

    private final SkolverketClient client;
    private final SkolverketProperties properties;
    private final JsonMapper jsonMapper;

    SkolverketDownloader(SkolverketClient client, SkolverketProperties properties, JsonMapper jsonMapper) {
        this.client = client;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    public void downloadTo(Path file) throws IOException {
        SkolverketSnapshot snapshot = download();
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), snapshot);
        log.info("Skrev {} skolor, {} program, {} kommuner till {}",
                snapshot.schools().size(), snapshot.programs().size(), snapshot.municipalities().size(),
                file.toAbsolutePath());
    }

    public SkolverketSnapshot download() {
        Instant startedAt = Instant.now();

        List<MunicipalityData> municipalities = withRetry("kommuner", client::getMunicipalities)
                .entrySet().stream()
                .map(e -> new MunicipalityData(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MunicipalityData::code))
                .toList();
        Map<String, String> statuses = withRetry("skolenhetsregistret", client::getSchoolUnitStatuses);
        List<ListedSchoolUnit> units = withRetry("skollista", client::listGymnasiumSchoolUnits);
        log.info("Hittade {} gymnasieskolor, hämtar detaljer...", units.size());

        Map<String, String> programNames = new ConcurrentHashMap<>();
        List<SchoolData> schools = fetchAllSchools(units, statuses, programNames);

        List<ProgramData> programs = programNames.entrySet().stream()
                .map(e -> new ProgramData(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ProgramData::code))
                .toList();

        return new SkolverketSnapshot(startedAt, municipalities, programs, schools);
    }

    private List<SchoolData> fetchAllSchools(List<ListedSchoolUnit> units, Map<String, String> statuses,
                                             Map<String, String> programNames) {
        Semaphore permits = new Semaphore(properties.concurrency());
        AtomicInteger done = new AtomicInteger();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<SchoolData>> futures = units.stream()
                    .map(unit -> executor.submit(() -> {
                        permits.acquire();
                        try {
                            return fetchSchool(unit.code(), statuses, programNames);
                        } finally {
                            permits.release();
                            int count = done.incrementAndGet();
                            if (count % 100 == 0 || count == units.size()) {
                                log.info("  {}/{} skolor", count, units.size());
                            }
                        }
                    }))
                    .toList();

            List<SchoolData> schools = new ArrayList<>();
            for (Future<SchoolData> future : futures) {
                schools.add(future.get());
            }
            schools.sort(Comparator.comparing(SchoolData::schoolUnitCode));
            return schools;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Nedladdningen avbröts", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Nedladdningen misslyckades", e.getCause());
        }
    }

    private SchoolData fetchSchool(String code, Map<String, String> statuses, Map<String, String> programNames) {
        SchoolUnitDetail detail = withRetry("skola " + code, () -> client.getSchoolUnit(code));
        List<EducationEvent> events = withRetry("utbildningar " + code, () -> client.getGymnasiumEducationEvents(code));

        List<SchoolProgramData> programs = events.stream()
                .peek(event -> programNames.putIfAbsent(event.studyPathCode(), event.studyPathName()))
                .map(SkolverketDownloader::toSchoolProgram)
                .sorted(Comparator.comparing(SchoolProgramData::programCode))
                .toList();

        Address visiting = visitingAddress(detail);
        return new SchoolData(
                code,
                detail.name(),
                detail.corporationName(),
                toOrganizerType(detail.principalOrganizerType()),
                blankToNull(detail.geographicalAreaCode()),
                visiting != null ? visiting.street() : null,
                visiting != null ? visiting.zipCode() : null,
                visiting != null ? visiting.city() : null,
                toCoordinate(detail.latitude()),
                toCoordinate(detail.longitude()),
                detail.contactInfo() != null ? blankToNull(detail.contactInfo().web()) : null,
                toStatus(statuses.get(code)),
                programs);
    }

    private static SchoolProgramData toSchoolProgram(EducationEvent event) {
        AdmissionPoints admission = AdmissionPoints.parse(
                event.admissionPointsMin(), event.admissionPointsAverage(), event.admissionPointsSemester());
        List<StudyPathData> studyPaths = event.studyPaths() == null ? List.of() : event.studyPaths().stream()
                .map(path -> new StudyPathData(path.studyPathCode(), path.studyPathName()))
                .distinct()
                .sorted(Comparator.comparing(StudyPathData::code))
                .toList();
        return new SchoolProgramData(event.studyPathCode(), admission.min(), admission.avg(),
                admission.year(), admission.note(), studyPaths);
    }

    private static Address visitingAddress(SchoolUnitDetail detail) {
        if (detail.contactInfo() == null || detail.contactInfo().addresses() == null) {
            return null;
        }
        return detail.contactInfo().addresses().stream()
                .filter(a -> "VISITING_ADDRESS".equals(a.type()))
                .findFirst()
                .orElse(null);
    }

    static OrganizerType toOrganizerType(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "Kommunal" -> OrganizerType.KOMMUNAL;
            case "Kommunalförbund" -> OrganizerType.KOMMUNALFORBUND;
            case "Region" -> OrganizerType.REGION;
            case "Fristående" -> OrganizerType.FRISTAENDE;
            default -> null;
        };
    }

    private static SchoolStatus toStatus(String value) {
        if (value == null) {
            return SchoolStatus.AKTIV; // Finns i Planerade utbildningar men inte i registret
        }
        return SchoolStatus.valueOf(value);
    }

    private static BigDecimal toCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value).setScale(6, java.math.RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static <T> T withRetry(String what, Supplier<T> call) {
        for (int attempt = 1; ; attempt++) {
            try {
                return call.get();
            } catch (RestClientException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException("Kunde inte hämta " + what + " efter " + attempt + " försök", e);
                }
                log.warn("Försök {} att hämta {} misslyckades: {}", attempt, what, e.getMessage());
                sleep(attempt * 1000L);
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
