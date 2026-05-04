package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record RevisionEvidenciaResponse(
        Long id,
        String statusCode,
        String statusName,
        String qualificationCode,
        String qualificationName,
        String comment,
        String correctiveActionDetail,
        String correctiveActionStatus,
        LocalDateTime reviewedAt
) {
}
