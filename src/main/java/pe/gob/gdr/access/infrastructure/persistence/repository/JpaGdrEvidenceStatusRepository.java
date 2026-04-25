package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;
import pe.gob.gdr.access.domain.repository.GdrEvidenceStatusRepository;

@Repository
public interface JpaGdrEvidenceStatusRepository
        extends JpaRepository<GdrEvidenceStatus, Long>, GdrEvidenceStatusRepository {

    @Override
    @Query("""
            select status
            from GdrEvidenceStatus status
            where upper(status.statusCode) = upper(:statusCode)
              and upper(status.status) = 'ACTIVE'
            """)
    Optional<GdrEvidenceStatus> findActiveByCode(@Param("statusCode") String statusCode);
}

