package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrImprovementStatus;
import pe.gob.gdr.access.domain.repository.GdrImprovementStatusRepository;

@Repository
public interface JpaGdrImprovementStatusRepository
        extends JpaRepository<GdrImprovementStatus, Long>, GdrImprovementStatusRepository {

    @Override
    @Query("""
            select status
            from GdrImprovementStatus status
            where upper(status.code) = upper(:statusCode)
              and upper(status.recordStatus) = 'ACTIVO'
            """)
    Optional<GdrImprovementStatus> findActiveByCode(@Param("statusCode") String statusCode);
}
