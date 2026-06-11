package pe.gob.gdr.access.domain.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.gdr.access.domain.model.AuditEvent;

public interface AuditEventRepository {

    AuditEvent save(AuditEvent auditEvent);

    Page<AuditEvent> search(
            String eventCode,
            String principal,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
