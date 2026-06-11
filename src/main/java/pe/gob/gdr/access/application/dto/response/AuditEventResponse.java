package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

/** Evento de auditoría consultable (trazabilidad institucional). */
public record AuditEventResponse(
        Long id,
        String eventCode,
        String principal,
        String detail,
        String clientIp,
        String requestPath,
        LocalDateTime occurredAt
) {
}
