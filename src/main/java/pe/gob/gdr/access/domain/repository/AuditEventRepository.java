package pe.gob.gdr.access.domain.repository;

import pe.gob.gdr.access.domain.model.AuditEvent;

public interface AuditEventRepository {

    AuditEvent save(AuditEvent auditEvent);
}
