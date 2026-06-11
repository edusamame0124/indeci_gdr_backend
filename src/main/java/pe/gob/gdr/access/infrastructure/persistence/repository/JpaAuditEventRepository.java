package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.AuditEvent;
import pe.gob.gdr.access.domain.repository.AuditEventRepository;

@Repository
public interface JpaAuditEventRepository extends JpaRepository<AuditEvent, Long>, AuditEventRepository {

    @Override
    @Query("""
            select e from AuditEvent e
            where (:eventCode is null or upper(e.eventCode) = upper(:eventCode))
              and (:principal is null or upper(e.principal) like upper(concat('%', :principal, '%')))
              and (:from is null or e.occurredAt >= :from)
              and (:to is null or e.occurredAt <= :to)
            order by e.occurredAt desc
            """)
    Page<AuditEvent> search(
            @Param("eventCode") String eventCode,
            @Param("principal") String principal,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
