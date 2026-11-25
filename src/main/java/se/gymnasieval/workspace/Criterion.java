package se.gymnasieval.workspace;

/** Kriterier som en bevakad skola kan få betyg 1–5 på. Standardvikterna kommer från originalappen. */
public enum Criterion {
    RESTID(3),
    INRIKTNING(3),
    POANG(2),
    RYKTE(2),
    PROFIL(2),
    MILJO(1),
    BESOK(3);

    static final int MIN_WEIGHT = 0;
    static final int MAX_WEIGHT = 5;
    static final int MIN_SCORE = 1;
    static final int MAX_SCORE = 5;

    private final int defaultWeight;

    Criterion(int defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public int defaultWeight() {
        return defaultWeight;
    }
}
