package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record GoalSummaryResponse(
        Long id,
        String title,
        BigDecimal expectedValue,
        BigDecimal weight,
        String status,
        Long assignmentId,
        String evaluatedName,
        Long indicatorId,
        String indicatorName
) {
}
