package pe.gob.gdr.access.domain.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoalScoringPolicy {

    public static final BigDecimal MAX_GOAL_SCORE = new BigDecimal("120");
    public static final BigDecimal MAX_FINAL_SCORE = new BigDecimal("120");

    public static final String SEGMENT_DIRECTIVO = "DIRECTIVO";
    public static final BigDecimal THRESHOLD_DIRECTIVO = new BigDecimal("70");
    public static final BigDecimal THRESHOLD_OTROS = new BigDecimal("60");

    private GoalScoringPolicy() {
    }

    public static QualitativeRating classifyRating(BigDecimal score, String segmentCode) {
        if (score == null) {
            return null;
        }
        BigDecimal threshold = SEGMENT_DIRECTIVO.equalsIgnoreCase(segmentCode)
                ? THRESHOLD_DIRECTIVO
                : THRESHOLD_OTROS;
        return score.compareTo(threshold) >= 0
                ? QualitativeRating.BUEN_RENDIMIENTO
                : QualitativeRating.SUJETO_OBSERVACION;
    }

    /**
     * Goal score used by Lot 3 / SERVIR-style weighting (see {@code GoalScoringPolicyTest} Table 7 cases).
     * <p>
     * Algebraically equals {@code (achievedValue / expectedValue) * 100 * (weight / 100)} when {@code weight}
     * is stored as percentage points (for example {@code 35} for 35%).
     * The implementation computes {@code achievedValue.multiply(weight).divide(expectedValue)} with rounding on
     * that single division—pedagogical spreadsheets may truncate the intermediate ratio differently.
     */
    public static BigDecimal calculateGoalScore(
            BigDecimal expectedValue,
            BigDecimal achievedValue,
            BigDecimal weight,
            int scale,
            RoundingMode rounding
    ) {
        BigDecimal proportional = achievedValue
                .multiply(weight)
                .divide(expectedValue, scale + 2, rounding);
        return proportional
                .min(MAX_GOAL_SCORE)
                .setScale(scale, rounding);
    }

    public static BigDecimal capFinalScore(BigDecimal score, int scale, RoundingMode rounding) {
        return score.min(MAX_FINAL_SCORE).setScale(scale, rounding);
    }
}
