package se.gymnasieval.skolverket;

import java.math.BigDecimal;

/**
 * Tolkar Skolverkets antagningspoäng, som kommer som text: "265,0", null eller fritext.
 * Tal blir BigDecimal; allt annat hamnar i {@code note}.
 */
record AdmissionPoints(BigDecimal min, BigDecimal avg, Short year, String note) {

    private static final int MAX_NOTE_LENGTH = 100;

    static AdmissionPoints parse(String min, String avg, String semester) {
        BigDecimal parsedMin = toNumber(min);
        String note = (parsedMin == null && min != null && !min.isBlank()) ? truncate(min.strip()) : null;
        return new AdmissionPoints(parsedMin, toNumber(avg), toYear(semester), note);
    }

    private static BigDecimal toNumber(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.strip().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Short toYear(String semester) {
        if (semester == null || !semester.strip().matches("\\d{4}")) {
            return null;
        }
        return Short.valueOf(semester.strip());
    }

    private static String truncate(String text) {
        return text.length() <= MAX_NOTE_LENGTH ? text : text.substring(0, MAX_NOTE_LENGTH);
    }
}
