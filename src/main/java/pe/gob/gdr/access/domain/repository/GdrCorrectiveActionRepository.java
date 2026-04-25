package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrCorrectiveAction;

public interface GdrCorrectiveActionRepository {

    Optional<GdrCorrectiveAction> findByReviewId(Long reviewId);

    List<GdrCorrectiveAction> findOpenByEvidenceId(Long evidenceId);

    GdrCorrectiveAction save(GdrCorrectiveAction action);
}
