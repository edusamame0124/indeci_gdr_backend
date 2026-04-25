package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record ResultadoResponse(
        Long resultId,
        Long assignmentId,
        Long evaluatedId,
        String evaluatedName,
        String evaluatorName,
        String cycleName,
        BigDecimal consolidatedScore,
        String status
) {
}
