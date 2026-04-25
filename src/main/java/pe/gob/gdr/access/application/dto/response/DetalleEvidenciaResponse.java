package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DetalleEvidenciaResponse(
        Long id,
        Long goalId,
        String goalTitle,
        String evaluatedName,
        String indicatorName,
        String title,
        String detail,
        LocalDate expectedDate,
        String statusCode,
        String statusName,
        String openCorrectiveActionDetail,
        String openCorrectiveActionStatus,
        List<RevisionEvidenciaResponse> reviews
) {
}
