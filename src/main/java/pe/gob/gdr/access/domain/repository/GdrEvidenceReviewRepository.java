package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvidenceReview;

public interface GdrEvidenceReviewRepository {

    List<GdrEvidenceReview> findByEvidenceId(Long evidenceId);

    Optional<GdrEvidenceReview> findLatestByEvidenceId(Long evidenceId);

    GdrEvidenceReview save(GdrEvidenceReview review);
}

