package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocFlowStatus;
import pe.gob.gdr.access.domain.repository.DocFlowStatusRepository;

@Repository
public interface JpaDocFlowStatusRepository extends JpaRepository<DocFlowStatus, Long>, DocFlowStatusRepository {

    @Override
    @Query("""
            select flowStatus
            from DocFlowStatus flowStatus
            where upper(flowStatus.code) = upper(:code)
              and upper(flowStatus.recordStatus) = 'ACTIVO'
            """)
    Optional<DocFlowStatus> findActiveByCode(@Param("code") String code);
}
