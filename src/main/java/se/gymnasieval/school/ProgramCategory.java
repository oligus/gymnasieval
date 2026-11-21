package se.gymnasieval.school;

import java.util.Set;

/** Grovindelning av program, för att kunna visa de vanliga programmen först. */
public enum ProgramCategory {
    /** De sex nationella högskoleförberedande programmen. */
    HOGSKOLEFORBEREDANDE,
    /** De tolv nationella yrkesprogrammen. */
    YRKES,
    /** Allt annat: riksrekryterande utbildningar, introduktionsprogram, IB m.m. */
    OVRIGA;

    private static final Set<String> HOGSKOLEFORBEREDANDE_CODES = Set.of("EK", "ES", "HU", "NA", "SA", "TE");
    private static final Set<String> YRKES_CODES =
            Set.of("BA", "BF", "EE", "FR", "FS", "FT", "HT", "IN", "NB", "RL", "VF", "VO");

    /** Kategori utifrån programkoden, t.ex. "TE25" → HOGSKOLEFORBEREDANDE. */
    static ProgramCategory of(String programCode) {
        if (programCode == null || !programCode.matches("[A-Z]{2}25")) {
            return OVRIGA; // nationella program har koder som TE25 (gymnasiereformen 2025)
        }
        String prefix = programCode.substring(0, 2);
        if (HOGSKOLEFORBEREDANDE_CODES.contains(prefix)) {
            return HOGSKOLEFORBEREDANDE;
        }
        return YRKES_CODES.contains(prefix) ? YRKES : OVRIGA;
    }
}
