package se.gymnasieval.workspace;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.gymnasieval.workspace.Criterion.BESOK;
import static se.gymnasieval.workspace.Criterion.INRIKTNING;
import static se.gymnasieval.workspace.Criterion.MILJO;
import static se.gymnasieval.workspace.Criterion.RESTID;

class TotalScoreTest {

    @Test
    void weightedAverageOfSetScores() {
        // RESTID 5 (vikt 3) och INRIKTNING 2 (vikt 1): (15 + 2) / 4 = 4,25
        TotalScore total = TotalScore.of(Map.of(RESTID, 5, INRIKTNING, 2), Map.of(RESTID, 3, INRIKTNING, 1));

        assertThat(total.value()).isCloseTo(4.25, within(0.0001));
        assertThat(total.missing()).isEqualTo(Criterion.values().length - 2);
    }

    @Test
    void missingScoresDoNotPullTheAverageDown() {
        TotalScore total = TotalScore.of(Map.of(RESTID, 4), Map.of());

        assertThat(total.value()).isEqualTo(4.0);
    }

    @Test
    void usesDefaultWeightsWhenNoneAreSet() {
        // Standardvikter: MILJO 1, BESOK 3 → (1*1 + 5*3) / 4 = 4,0
        TotalScore total = TotalScore.of(Map.of(MILJO, 1, BESOK, 5), Map.of());

        assertThat(total.value()).isEqualTo(4.0);
    }

    @Test
    void noScoresGivesNoValue() {
        TotalScore total = TotalScore.of(Map.of(), Map.of());

        assertThat(total.value()).isNull();
        assertThat(total.missing()).isEqualTo(Criterion.values().length);
    }

    @Test
    void criteriaWithWeightZeroAreIgnored() {
        TotalScore total = TotalScore.of(Map.of(RESTID, 1, INRIKTNING, 5), Map.of(RESTID, 0, INRIKTNING, 2));

        assertThat(total.value()).isEqualTo(5.0);
    }

    @Test
    void allWeightsZeroGivesNoValue() {
        TotalScore total = TotalScore.of(Map.of(RESTID, 3), Map.of(RESTID, 0));

        assertThat(total.value()).isNull();
        assertThat(total.missing()).isEqualTo(Criterion.values().length - 1);
    }
}
