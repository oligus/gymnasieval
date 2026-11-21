package se.gymnasieval.school;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramCategoryTest {

    @Test
    void categorizesNationalPrograms() {
        assertThat(ProgramCategory.of("TE25")).isEqualTo(ProgramCategory.HOGSKOLEFORBEREDANDE);
        assertThat(ProgramCategory.of("NA25")).isEqualTo(ProgramCategory.HOGSKOLEFORBEREDANDE);
        assertThat(ProgramCategory.of("VO25")).isEqualTo(ProgramCategory.YRKES);
        assertThat(ProgramCategory.of("FR25")).isEqualTo(ProgramCategory.YRKES);
    }

    @Test
    void everythingElseIsOther() {
        assertThat(ProgramCategory.of("FL25")).isEqualTo(ProgramCategory.OVRIGA); // flygteknik, riksrekryterande
        assertThat(ProgramCategory.of("IMVHTG")).isEqualTo(ProgramCategory.OVRIGA); // introduktionsprogram
        assertThat(ProgramCategory.of("IBCP0")).isEqualTo(ProgramCategory.OVRIGA);
        assertThat(ProgramCategory.of(null)).isEqualTo(ProgramCategory.OVRIGA);
    }

    @Test
    void countyFromMunicipalityCode() {
        assertThat(Counties.nameForMunicipality("0114")).isEqualTo("Stockholms län");
        assertThat(Counties.nameForMunicipality("1480")).isEqualTo("Västra Götalands län");
        assertThat(Counties.nameForMunicipality("2584")).isEqualTo("Norrbottens län");
        assertThat(Counties.nameForMunicipality(null)).isNull();
    }
}
