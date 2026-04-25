package pe.gob.gdr.access.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.AuditEvent;
import pe.gob.gdr.access.domain.repository.AuditEventRepository;

@Repository
public interface JpaAuditEventRepository extends JpaRepository<AuditEvent, Long>, AuditEventRepository {
}
