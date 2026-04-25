package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrCorrectiveAction;
import pe.gob.gdr.access.domain.repository.GdrCorrectiveActionRepository;

@Repository
public interface JpaGdrCorrectiveActionRepository
        extends JpaRepository<GdrCorrectiveAction, Long>, GdrCorrectiveActionRepository {

    @Override
    @Query("""
            select action
            from GdrCorrectiveAction action
            join fetch action.evidenceReview review
            where review.id = :reviewId
            """)
    Optional<GdrCorrectiveAction> findByReviewId(@Param("reviewId") Long reviewId);

    @Override
    @Query("""
            select action
            from GdrCorrectiveAction action
            join fetch action.evidence evidence
            join fetch action.evidenceReview review
            where evidence.id = :evidenceId
              and upper(action.actionStatus) = 'OPEN'
            order by action.createdAt desc, action.id desc
            """)
    List<GdrCorrectiveAction> findOpenByEvidenceId(@Param("evidenceId") Long evidenceId);
}
