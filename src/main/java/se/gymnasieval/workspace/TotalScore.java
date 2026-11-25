package se.gymnasieval.workspace;

import java.util.Map;

/**
 * Totalpoängen för en skola: viktat medelvärde av de betyg som är satta.
 * Kriterier utan betyg räknas inte (de drar inte ner snittet), men räknas som saknade.
 *
 * @param value   0–5, eller null om inget betyg med vikt över 0 finns
 * @param missing antal kriterier som saknar betyg
 */
public record TotalScore(Double value, int missing) {

    public static TotalScore of(Map<Criterion, Integer> scores, Map<Criterion, Integer> weights) {
        double weightedSum = 0;
        int weightSum = 0;
        int missing = 0;
        for (Criterion criterion : Criterion.values()) {
            Integer score = scores.get(criterion);
            if (score == null) {
                missing++;
                continue;
            }
            int weight = weights.getOrDefault(criterion, criterion.defaultWeight());
            weightedSum += weight * score;
            weightSum += weight;
        }
        return new TotalScore(weightSum == 0 ? null : weightedSum / weightSum, missing);
    }
}
