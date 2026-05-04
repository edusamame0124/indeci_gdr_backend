package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalDetailResponse(
        Long id,
        Long assignmentId,
        String cycleName,
        String evaluatorName,
        String evaluatedName,
        Long indicatorId,
        String indicatorCode,
        String indicatorName,
        String title,
        String description,
        BigDecimal expectedValue,
        BigDecimal weight,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal achievedValue,
        BigDecimal calculatedScore
) {
}
