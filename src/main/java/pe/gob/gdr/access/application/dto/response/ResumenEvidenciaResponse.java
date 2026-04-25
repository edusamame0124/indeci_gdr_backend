package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;

public record ResumenEvidenciaResponse(
        Long id,
        Long goalId,
        String title,
        String detail,
        LocalDate expectedDate,
        String statusCode,
        String statusName,
        String latestReviewComment
) {
}
