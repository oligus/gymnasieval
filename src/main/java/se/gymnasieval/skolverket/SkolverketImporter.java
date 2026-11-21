package se.gymnasieval.skolverket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gymnasieval.school.Municipality;
import se.gymnasieval.school.MunicipalityRepository;
import se.gymnasieval.school.Program;
import se.gymnasieval.school.ProgramRepository;
import se.gymnasieval.school.School;
import se.gymnasieval.school.SchoolProgram;
import se.gymnasieval.school.SchoolRepository;
import se.gymnasieval.school.StudyPath;
import se.gymnasieval.skolverket.SkolverketSnapshot.MunicipalityData;
import se.gymnasieval.skolverket.SkolverketSnapshot.ProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolProgramData;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Skriver en {@link SkolverketSnapshot} till databasen.
 * Upsert på Skolverkets koder, så importen kan köras om hur många gånger som helst.
 * Skolor tas aldrig bort, eftersom användardata kan peka på dem.
 */
@Service
public class SkolverketImporter {

    private static final Logger log = LoggerFactory.getLogger(SkolverketImporter.class);

    private final SchoolRepository schools;
    private final ProgramRepository programs;
    private final MunicipalityRepository municipalities;

    SkolverketImporter(SchoolRepository schools, ProgramRepository programs, MunicipalityRepository municipalities) {
        this.schools = schools;
        this.programs = programs;
        this.municipalities = municipalities;
    }

    public record ImportResult(int schoolsCreated, int schoolsUpdated, int programs, int municipalities) {}

    /** Allt i en transaktion: går något fel rullas hela importen tillbaka. */
    @Transactional
    public ImportResult importSnapshot(SkolverketSnapshot snapshot) {
        Map<String, Municipality> municipalityByCode = upsertMunicipalities(snapshot);
        Map<String, Program> programByCode = upsertPrograms(snapshot);
        Map<String, School> existingSchools = schools.findAll().stream()
                .collect(Collectors.toMap(School::getSchoolUnitCode, Function.identity()));

        Instant importedAt = Instant.now();
        int created = 0;
        for (SchoolData data : snapshot.schools()) {
            School school = existingSchools.get(data.schoolUnitCode());
            if (school == null) {
                school = schools.save(new School(data.schoolUnitCode(), data.name(), data.status(), importedAt));
                created++;
            }
            updateSchool(school, data, importedAt, municipalityByCode, programByCode);
        }

        ImportResult result = new ImportResult(created, snapshot.schools().size() - created,
                programByCode.size(), municipalityByCode.size());
        log.info("Import klar: {} nya skolor, {} uppdaterade, {} program, {} kommuner",
                result.schoolsCreated(), result.schoolsUpdated(), result.programs(), result.municipalities());
        return result;
    }

    private Map<String, Municipality> upsertMunicipalities(SkolverketSnapshot snapshot) {
        Map<String, Municipality> byCode = municipalities.findAll().stream()
                .collect(Collectors.toMap(Municipality::getCode, Function.identity()));
        for (MunicipalityData data : snapshot.municipalities()) {
            Municipality existing = byCode.get(data.code());
            if (existing == null) {
                byCode.put(data.code(), municipalities.save(new Municipality(data.code(), data.name())));
            } else {
                existing.setName(data.name());
            }
        }
        return byCode;
    }

    private Map<String, Program> upsertPrograms(SkolverketSnapshot snapshot) {
        Map<String, Program> byCode = programs.findAll().stream()
                .collect(Collectors.toMap(Program::getCode, Function.identity()));
        for (ProgramData data : snapshot.programs()) {
            Program existing = byCode.get(data.code());
            if (existing == null) {
                byCode.put(data.code(), programs.save(new Program(data.code(), data.name())));
            } else {
                existing.setName(data.name());
            }
        }
        return byCode;
    }

    // Ingen save() behövs för befintliga skolor: Hibernate upptäcker ändringar vid commit (dirty checking).
    private void updateSchool(School school, SchoolData data, Instant importedAt,
                              Map<String, Municipality> municipalityByCode, Map<String, Program> programByCode) {
        school.setName(data.name());
        school.setOrganizerName(data.organizerName());
        school.setOrganizerType(data.organizerType());
        school.setMunicipality(lookupMunicipality(data, municipalityByCode));
        school.setStreet(data.street());
        school.setZipCode(data.zipCode());
        school.setCity(data.city());
        school.setLatitude(data.latitude());
        school.setLongitude(data.longitude());
        school.setWebUrl(data.webUrl());
        school.setStatus(data.status());
        school.setImportedAt(importedAt);

        Set<String> programCodes = data.programs().stream()
                .map(SchoolProgramData::programCode)
                .collect(Collectors.toSet());
        school.retainPrograms(programCodes);

        for (SchoolProgramData programData : data.programs()) {
            Program program = programByCode.get(programData.programCode());
            SchoolProgram schoolProgram = school.programFor(program);
            schoolProgram.setAdmission(programData.admissionPointsMin(), programData.admissionPointsAvg(),
                    programData.admissionYear(), programData.admissionNote());
            schoolProgram.replaceStudyPaths(programData.studyPaths().stream()
                    .map(path -> new StudyPath(path.code(), path.name()))
                    .collect(Collectors.toSet()));
        }
    }

    private static Municipality lookupMunicipality(SchoolData data, Map<String, Municipality> municipalityByCode) {
        if (data.municipalityCode() == null) {
            return null;
        }
        Municipality municipality = municipalityByCode.get(data.municipalityCode());
        if (municipality == null) {
            log.warn("Okänd kommunkod {} för skola {}", data.municipalityCode(), data.schoolUnitCode());
        }
        return municipality;
    }
}
