package se.gymnasieval.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import se.gymnasieval.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class SchoolPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void savesAndLoadsSchoolWithProgramsAndStudyPaths() {
        Municipality vasby = em.persist(new Municipality("0114", "Upplands Väsby"));
        Program teknik = em.persist(new Program("TE25", "Teknikprogrammet"));

        School school = new School("12345678", "Testgymnasiet", SchoolStatus.AKTIV, Instant.now());
        school.setMunicipality(vasby);
        school.setOrganizerType(OrganizerType.KOMMUNAL);
        school.setLatitude(new BigDecimal("59.518000"));

        SchoolProgram te = school.addProgram(teknik);
        te.setAdmission(new BigDecimal("242.5"), new BigDecimal("265.0"), (short) 2026, null);
        te.addStudyPath("TEINF", "Informations- och medieteknik");
        te.addStudyPath("TETEK", "Teknikvetenskap");

        em.persist(school);
        em.flush();
        em.clear(); // töm cachen så att nästa find verkligen läser från databasen

        School loaded = em.find(School.class, "12345678");

        assertThat(loaded.getName()).isEqualTo("Testgymnasiet");
        assertThat(loaded.getMunicipality().getName()).isEqualTo("Upplands Väsby");
        assertThat(loaded.getOrganizerType()).isEqualTo(OrganizerType.KOMMUNAL);
        assertThat(loaded.getPrograms()).singleElement().satisfies(p -> {
            assertThat(p.getProgram().getName()).isEqualTo("Teknikprogrammet");
            assertThat(p.getAdmissionPointsMin()).isEqualByComparingTo("242.5");
            assertThat(p.getStudyPaths()).extracting(StudyPath::code)
                    .containsExactlyInAnyOrder("TEINF", "TETEK");
        });
    }
}
