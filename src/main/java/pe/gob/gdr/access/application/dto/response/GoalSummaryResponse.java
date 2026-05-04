package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalSummaryResponse(
        Long id,
        String title,
        BigDecimal expectedValue,
        BigDecimal weight,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long assignmentId,
        String evaluatedName,
        Long indicatorId,
        String indicatorName,
        long evidenceCount,
        BigDecimal achievedValue,
        BigDecimal calculatedScore
) {
}
