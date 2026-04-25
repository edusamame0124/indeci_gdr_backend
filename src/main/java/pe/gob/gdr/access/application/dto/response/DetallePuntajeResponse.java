package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record DetallePuntajeResponse(
        Long goalId,
        String goalTitle,
        String indicatorName,
        BigDecimal expectedValue,
        BigDecimal weight,
        BigDecimal achievedValue,
        BigDecimal scoreValue,
        String detailComment
) {
}
