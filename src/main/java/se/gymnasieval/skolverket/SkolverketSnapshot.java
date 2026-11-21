package se.gymnasieval.skolverket;

import se.gymnasieval.school.OrganizerType;
import se.gymnasieval.school.SchoolStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Vårt eget filformat för en nedladdning från Skolverket (data/skolverket.json).
 * Är kontraktet mellan kommandona skolverket:download och skolverket:import.
 * Fälten följer vår domän, inte Skolverkets API.
 */
public record SkolverketSnapshot(
        Instant downloadedAt,
        List<MunicipalityData> municipalities,
        List<ProgramData> programs,
        List<SchoolData> schools) {

    public record MunicipalityData(String code, String name) {}

    public record ProgramData(String code, String name) {}

    public record SchoolData(
            String schoolUnitCode,
            String name,
            String organizerName,
            OrganizerType organizerType,
            String municipalityCode,
            String street,
            String zipCode,
            String city,
            BigDecimal latitude,
            BigDecimal longitude,
            String webUrl,
            SchoolStatus status,
            List<SchoolProgramData> programs) {}

    public record SchoolProgramData(
            String programCode,
            BigDecimal admissionPointsMin,
            BigDecimal admissionPointsAvg,
            Short admissionYear,
            String admissionNote,
            List<StudyPathData> studyPaths) {}

    public record StudyPathData(String code, String name) {}
}
