package pe.gob.gdr.access.application.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.response.AuditEventResponse;
import pe.gob.gdr.access.application.dto.response.PageResponse;
import pe.gob.gdr.access.domain.model.AuditEvent;
import pe.gob.gdr.access.domain.repository.AuditEventRepository;

@Service
public class AuditTrailQueryService {

    private final AuditEventRepository auditEventRepository;
    private final AuditTrailService auditTrailService;

    public AuditTrailQueryService(AuditEventRepository auditEventRepository, AuditTrailService auditTrailService) {
        this.auditEventRepository = auditEventRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> buscar(
            String eventCode,
            String principal,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        Page<AuditEvent> page = auditEventRepository.search(
                normalize(eventCode),
                normalize(principal),
                from,
                to,
                pageable
        );
        return PageResponse.from(page.map(this::mapEvent));
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(
            String eventCode,
            String principal,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable,
            String username
    ) {
        PageResponse<AuditEventResponse> page = buscar(eventCode, principal, from, to, pageable);
        auditTrailService.recordEvent(
                "AUDIT_TRAIL_EXPORTADO",
                username,
                "Exportación CSV de auditoría (" + page.content().size() + " filas).",
                null
        );
        return toCsv(page.content());
    }

    private AuditEventResponse mapEvent(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getEventCode(),
                event.getPrincipal(),
                event.getDetail(),
                event.getClientIp(),
                event.getRequestPath(),
                event.getOccurredAt()
        );
    }

    private byte[] toCsv(java.util.List<AuditEventResponse> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("id,eventCode,principal,detail,clientIp,requestPath,occurredAt\n");
        for (AuditEventResponse row : rows) {
            builder.append(row.id()).append(',')
                    .append(escape(row.eventCode())).append(',')
                    .append(escape(row.principal())).append(',')
                    .append(escape(row.detail())).append(',')
                    .append(escape(row.clientIp())).append(',')
                    .append(escape(row.requestPath())).append(',')
                    .append(escape(row.occurredAt() == null ? "" : row.occurredAt().toString()))
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
