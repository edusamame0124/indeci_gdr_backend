package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrImprovementStatus;

public interface GdrImprovementStatusRepository {

    Optional<GdrImprovementStatus> findActiveByCode(String statusCode);

    GdrImprovementStatus save(GdrImprovementStatus status);
}
