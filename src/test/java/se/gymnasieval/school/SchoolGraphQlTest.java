package se.gymnasieval.school;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import se.gymnasieval.TestcontainersConfiguration;
import se.gymnasieval.skolverket.SkolverketImporter;
import se.gymnasieval.skolverket.SkolverketSnapshot;
import se.gymnasieval.skolverket.SkolverketSnapshot.MunicipalityData;
import se.gymnasieval.skolverket.SkolverketSnapshot.ProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.StudyPathData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Kör GraphQL-frågor genom hela kedjan ner till MySQL.
 * Avsiktligt utan @Transactional: då upptäcks lat laddning utanför transaktion,
 * precis som i den riktiga appen.
 */
@SpringBootTest
@AutoConfigureGraphQlTester
@Import(TestcontainersConfiguration.class)
class SchoolGraphQlTest {

    @Autowired
    private GraphQlTester graphQl;

    @Autowired
    private SkolverketImporter importer;

    @BeforeEach
    void importTestData() {
        // Importen är en upsert, så den kan köras före varje test.
        importer.importSnapshot(new SkolverketSnapshot(Instant.now(),
                List.of(new MunicipalityData("0114", "Upplands Väsby"),
                        new MunicipalityData("0160", "Täby")),
                List.of(new ProgramData("TE25", "Teknikprogrammet"),
                        new ProgramData("NA25", "Naturvetenskapsprogrammet")),
                List.of(school("11111111", "Väsby Nya Gymnasium", "0114",
                                program("TE25", "225.0", "TEINF", "TETEK"),
                                program("NA25", "260.0", "NANAP")),
                        school("22222222", "Tibble Gymnasium", "0160",
                                program("TE25", "282.5", "TEINF")),
                        school("33333333", "Naturskolan", "0160",
                                program("NA25", "300.0", "NANAP")))));
    }

    @Test
    void schoolsWithProgramAreSortedByName() {
        graphQl.document("""
                        { schools(program: "TE25") { code name municipality { name } } }
                        """)
                .execute()
                .path("schools[*].name").entityList(String.class)
                .containsExactly("Tibble Gymnasium", "Väsby Nya Gymnasium")
                .path("schools[1].municipality.name").entity(String.class).isEqualTo("Upplands Väsby");
    }

    @Test
    void schoolsCanBeFilteredByMunicipality() {
        graphQl.document("""
                        { schools(program: "TE25", municipality: "0114") { code } }
                        """)
                .execute()
                .path("schools[*].code").entityList(String.class).containsExactly("11111111");
    }

    @Test
    void schoolIncludesProgramsWithPointsAndStudyPaths() {
        graphQl.document("""
                        {
                          school(code: "11111111") {
                            name
                            municipality { code }
                            programs {
                              program { code name }
                              admissionPointsMin
                              admissionYear
                              studyPaths { code }
                            }
                          }
                        }
                        """)
                .execute()
                .path("school.municipality.code").entity(String.class).isEqualTo("0114")
                // Sorterat på programnamn: Naturvetenskap före Teknik
                .path("school.programs[*].program.code").entityList(String.class).containsExactly("NA25", "TE25")
                .path("school.programs[1].program.name").entity(String.class).isEqualTo("Teknikprogrammet")
                .path("school.programs[1].admissionPointsMin").entity(Double.class).isEqualTo(225.0)
                .path("school.programs[1].admissionYear").entity(Integer.class).isEqualTo(2026)
                .path("school.programs[1].studyPaths[*].code").entityList(String.class)
                .hasSize(2).contains("TEINF", "TETEK");
    }

    @Test
    void unknownSchoolGivesNull() {
        graphQl.document("{ school(code: \"99999999\") { name } }")
                .execute()
                .path("school").valueIsNull();
    }

    @Test
    void programsAndMunicipalitiesAreSortedByName() {
        graphQl.document("{ programs { code } municipalities { name } }")
                .execute()
                .path("programs[*].code").entityList(String.class).containsExactly("NA25", "TE25")
                .path("municipalities[*].name").entityList(String.class).containsExactly("Täby", "Upplands Väsby");
    }

    private static SchoolData school(String code, String name, String municipality, SchoolProgramData... programs) {
        return new SchoolData(code, name, null, OrganizerType.KOMMUNAL, municipality, null, null, null,
                null, null, null, SchoolStatus.AKTIV, List.of(programs));
    }

    private static SchoolProgramData program(String code, String minPoints, String... studyPaths) {
        return new SchoolProgramData(code, new BigDecimal(minPoints), null, (short) 2026, null,
                java.util.Arrays.stream(studyPaths).map(p -> new StudyPathData(p, "Inriktning " + p)).toList());
    }
}
