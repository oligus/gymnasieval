package se.gymnasieval.skolverket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import se.gymnasieval.TestcontainersConfiguration;
import se.gymnasieval.school.OrganizerType;
import se.gymnasieval.school.School;
import se.gymnasieval.school.SchoolProgram;
import se.gymnasieval.school.SchoolStatus;
import se.gymnasieval.school.StudyPath;
import se.gymnasieval.skolverket.SkolverketImporter.ImportResult;
import se.gymnasieval.skolverket.SkolverketSnapshot.MunicipalityData;
import se.gymnasieval.skolverket.SkolverketSnapshot.ProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.StudyPathData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, SkolverketImporter.class})
class SkolverketImporterTest {

    @Autowired
    private SkolverketImporter importer;

    @Autowired
    private TestEntityManager em;

    @Test
    void createsSchoolsProgramsAndStudyPaths() {
        ImportResult result = importer.importSnapshot(snapshot(
                program("TE25", "242,5", List.of(path("TEINF"), path("TETEK"))),
                program("NA25", "280,0", List.of(path("NANAP")))));

        assertThat(result.schoolsCreated()).isEqualTo(1);
        School school = reload();
        assertThat(school.getName()).isEqualTo("Testgymnasiet");
        assertThat(school.getMunicipality().getName()).isEqualTo("Upplands Väsby");
        assertThat(school.getPrograms()).hasSize(2);
        assertThat(program(school, "TE25").getStudyPaths()).extracting(StudyPath::code)
                .containsExactlyInAnyOrder("TEINF", "TETEK");
    }

    @Test
    void reimportUpdatesPointsAndRemovesDroppedPrograms() {
        importer.importSnapshot(snapshot(
                program("TE25", "242,5", List.of(path("TEINF"), path("TETEK"))),
                program("NA25", "280,0", List.of(path("NANAP")))));
        em.flush();
        em.clear();

        // Andra året: NA läggs ner, TE får ny poäng och tappar en inriktning.
        ImportResult result = importer.importSnapshot(snapshot(
                program("TE25", "250,0", List.of(path("TEINF")))));

        assertThat(result.schoolsCreated()).isZero();
        assertThat(result.schoolsUpdated()).isEqualTo(1);
        School school = reload();
        assertThat(school.getPrograms()).extracting(p -> p.getId().programCode()).containsExactly("TE25");
        SchoolProgram te = program(school, "TE25");
        assertThat(te.getAdmissionPointsMin()).isEqualByComparingTo("250.0");
        assertThat(te.getStudyPaths()).extracting(StudyPath::code).containsExactly("TEINF");
    }

    private School reload() {
        em.flush();
        em.clear();
        return em.find(School.class, "12345678");
    }

    private static SchoolProgram program(School school, String code) {
        return school.getPrograms().stream()
                .filter(p -> p.getId().programCode().equals(code))
                .findFirst()
                .orElseThrow();
    }

    private static SkolverketSnapshot snapshot(SchoolProgramData... programs) {
        SchoolData school = new SchoolData("12345678", "Testgymnasiet", "Upplands Väsby kommun",
                OrganizerType.KOMMUNAL, "0114", "Skolvägen 1", "19430", "Upplands Väsby",
                new BigDecimal("59.518000"), new BigDecimal("17.911000"), "https://example.se",
                SchoolStatus.AKTIV, List.of(programs));
        return new SkolverketSnapshot(Instant.now(),
                List.of(new MunicipalityData("0114", "Upplands Väsby")),
                List.of(new ProgramData("TE25", "Teknikprogrammet"),
                        new ProgramData("NA25", "Naturvetenskapsprogrammet")),
                List.of(school));
    }

    private static SchoolProgramData program(String code, String minPoints, List<StudyPathData> paths) {
        AdmissionPoints points = AdmissionPoints.parse(minPoints, null, "2026");
        return new SchoolProgramData(code, points.min(), points.avg(), points.year(), points.note(), paths);
    }

    private static StudyPathData path(String code) {
        return new StudyPathData(code, "Inriktning " + code);
    }
}
