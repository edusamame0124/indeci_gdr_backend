package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvidenceReview;
import pe.gob.gdr.access.domain.repository.GdrEvidenceReviewRepository;

@Repository
public interface JpaGdrEvidenceReviewRepository
        extends JpaRepository<GdrEvidenceReview, Long>, GdrEvidenceReviewRepository {

    @Query("""
            select review
            from GdrEvidenceReview review
            join fetch review.evidence evidence
            join fetch review.evidenceStatus evidenceStatus
            where evidence.id = :evidenceId
            order by review.reviewedAt desc, review.id desc
            """)
    List<GdrEvidenceReview> findByEvidenceId(@Param("evidenceId") Long evidenceId);

    @Query("""
            select review
            from GdrEvidenceReview review
            join fetch review.evidence evidence
            join fetch review.evidenceStatus evidenceStatus
            where evidence.id = :evidenceId
            order by review.reviewedAt desc, review.id desc
            """)
    List<GdrEvidenceReview> findLatestCandidates(@Param("evidenceId") Long evidenceId);

    @Override
    default Optional<GdrEvidenceReview> findLatestByEvidenceId(Long evidenceId) {
        return findLatestCandidates(evidenceId).stream().findFirst();
    }
}
