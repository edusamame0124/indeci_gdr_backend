package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;

public interface GdrEvidenceStatusRepository {

    Optional<GdrEvidenceStatus> findActiveByCode(String statusCode);
}

