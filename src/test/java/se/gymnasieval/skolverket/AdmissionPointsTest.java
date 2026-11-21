package se.gymnasieval.skolverket;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmissionPointsTest {

    @Test
    void parsesSwedishDecimalNumbers() {
        AdmissionPoints points = AdmissionPoints.parse("265,0", "284,5", "2026");

        assertThat(points.min()).isEqualByComparingTo("265.0");
        assertThat(points.avg()).isEqualByComparingTo("284.5");
        assertThat(points.year()).isEqualTo((short) 2026);
        assertThat(points.note()).isNull();
    }

    @Test
    void keepsNonNumericValueAsNote() {
        AdmissionPoints points = AdmissionPoints.parse("alla antagna", null, "2026");

        assertThat(points.min()).isNull();
        assertThat(points.avg()).isNull();
        assertThat(points.note()).isEqualTo("alla antagna");
    }

    @Test
    void handlesMissingValues() {
        AdmissionPoints points = AdmissionPoints.parse(null, null, null);

        assertThat(points).isEqualTo(new AdmissionPoints(null, null, null, null));
    }
}
