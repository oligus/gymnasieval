package se.gymnasieval.skolverket;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP-klient mot Skolverkets API:er. Records nedan speglar Skolverkets JSON-format
 * och används bara inom den här modulen; resten av appen ser dem aldrig.
 */
@Component
class SkolverketClient {

    private static final String PLANNED_EDUCATIONS_JSON =
            "application/vnd.skolverket.plannededucations.api.v3.hal+json";
    private static final int PAGE_SIZE = 100; // API:ets maxstorlek för school-units

    private final RestClient plannedEducations;
    private final RestClient registry;

    SkolverketClient(RestClient.Builder builder, SkolverketProperties properties) {
        this.plannedEducations = builder.clone()
                .baseUrl(properties.plannedEducationsUrl())
                .defaultHeader(HttpHeaders.ACCEPT, PLANNED_EDUCATIONS_JSON)
                .build();
        this.registry = builder.clone()
                .baseUrl(properties.registryUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    /** Alla skolenheter med gymnasieskola (sidindelat i API:et). */
    List<ListedSchoolUnit> listGymnasiumSchoolUnits() {
        List<ListedSchoolUnit> units = new ArrayList<>();
        int page = 0;
        int totalPages;
        do {
            SchoolUnitPage response = plannedEducations.get()
                    .uri("/school-units?typeOfSchooling=gy&size={size}&page={page}", PAGE_SIZE, page)
                    .retrieve()
                    .body(SchoolUnitPage.class);
            units.addAll(response.body().embedded().listedSchoolUnits());
            totalPages = response.body().page().totalPages();
            page++;
        } while (page < totalPages);
        return units;
    }

    SchoolUnitDetail getSchoolUnit(String schoolUnitCode) {
        return plannedEducations.get()
                .uri("/school-units/{code}", schoolUnitCode)
                .retrieve()
                .body(SchoolUnitResponse.class)
                .body();
    }

    List<EducationEvent> getGymnasiumEducationEvents(String schoolUnitCode) {
        EducationEventsResponse response = plannedEducations.get()
                .uri("/school-units/{code}/education-events", schoolUnitCode)
                .retrieve()
                .body(EducationEventsResponse.class);
        List<EducationEvent> gy = response.body().gy();
        return gy != null ? gy : List.of();
    }

    /** Kommunkod → kommunnamn. */
    Map<String, String> getMunicipalities() {
        return plannedEducations.get()
                .uri("/support/geographical-areas")
                .retrieve()
                .body(GeographicalAreasResponse.class)
                .body().stream()
                .filter(area -> "MUNICIPALITY".equals(area.areaType()))
                .collect(Collectors.toMap(GeographicalArea::code, GeographicalArea::name, (a, b) -> a));
    }

    /** Skolenhetskod → status (AKTIV, VILANDE, ...) från skolenhetsregistret. Ett enda anrop. */
    Map<String, String> getSchoolUnitStatuses() {
        return registry.get()
                .uri("/school-units")
                .retrieve()
                .body(RegistryResponse.class)
                .data().attributes().stream()
                .collect(Collectors.toMap(RegistryUnit::schoolUnitCode, RegistryUnit::status, (a, b) -> a));
    }

    // ---- Skolverkets JSON-format -------------------------------------------------

    record SchoolUnitPage(SchoolUnitPageBody body) {}

    record SchoolUnitPageBody(@JsonProperty("_embedded") SchoolUnitList embedded, PageInfo page) {}

    record SchoolUnitList(List<ListedSchoolUnit> listedSchoolUnits) {}

    record PageInfo(int totalPages, int totalElements) {}

    record ListedSchoolUnit(String code, String name) {}

    record SchoolUnitResponse(SchoolUnitDetail body) {}

    record SchoolUnitDetail(
            String code,
            String name,
            ContactInfo contactInfo,
            String geographicalAreaCode,
            @JsonProperty("wgs84_Lat") String latitude,
            @JsonProperty("wgs84_Long") String longitude,
            String principalOrganizerType,
            String corporationName) {}

    record ContactInfo(String web, List<Address> addresses) {}

    record Address(String type, String street, String zipCode, String city) {}

    record EducationEventsResponse(EducationEventsBody body) {}

    record EducationEventsBody(List<EducationEvent> gy) {}

    record EducationEvent(
            String studyPathCode,
            String studyPathName,
            List<StudyPath> studyPaths,
            String admissionPointsMin,
            String admissionPointsAverage,
            String admissionPointsSemester) {}

    record StudyPath(String studyPathCode, String studyPathName) {}

    record GeographicalAreasResponse(List<GeographicalArea> body) {}

    record GeographicalArea(String code, String name, String areaType) {}

    record RegistryResponse(RegistryData data) {}

    record RegistryData(List<RegistryUnit> attributes) {}

    record RegistryUnit(String schoolUnitCode, String name, String status) {}
}
