package se.gymnasieval.school;

import java.util.Map;

/** Län utifrån kommunkoden: de två första siffrorna är länskoden (SCB), t.ex. 0114 → 01 Stockholms län. */
final class Counties {

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("01", "Stockholms län"),
            Map.entry("03", "Uppsala län"),
            Map.entry("04", "Södermanlands län"),
            Map.entry("05", "Östergötlands län"),
            Map.entry("06", "Jönköpings län"),
            Map.entry("07", "Kronobergs län"),
            Map.entry("08", "Kalmar län"),
            Map.entry("09", "Gotlands län"),
            Map.entry("10", "Blekinge län"),
            Map.entry("12", "Skåne län"),
            Map.entry("13", "Hallands län"),
            Map.entry("14", "Västra Götalands län"),
            Map.entry("17", "Värmlands län"),
            Map.entry("18", "Örebro län"),
            Map.entry("19", "Västmanlands län"),
            Map.entry("20", "Dalarnas län"),
            Map.entry("21", "Gävleborgs län"),
            Map.entry("22", "Västernorrlands län"),
            Map.entry("23", "Jämtlands län"),
            Map.entry("24", "Västerbottens län"),
            Map.entry("25", "Norrbottens län"));

    private Counties() {
    }

    static String nameForMunicipality(String municipalityCode) {
        if (municipalityCode == null || municipalityCode.length() < 2) {
            return null;
        }
        return NAMES.get(municipalityCode.substring(0, 2));
    }
}
