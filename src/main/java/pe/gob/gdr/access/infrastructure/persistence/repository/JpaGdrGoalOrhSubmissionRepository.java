package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrGoalOrhSubmission;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;
import pe.gob.gdr.access.domain.repository.GdrGoalOrhSubmissionRepository;

@Repository
public interface JpaGdrGoalOrhSubmissionRepository
        extends JpaRepository<GdrGoalOrhSubmission, Long>, GdrGoalOrhSubmissionRepository {

    @Override
    @Query("""
            select count(submission) > 0
            from GdrGoalOrhSubmission submission
            where submission.goal.id = :goalId
              and submission.status = :status
              and upper(submission.recordStatus) = 'ACTIVO'
            """)
    boolean existsActiveSubmission(
            @Param("goalId") Long goalId,
            @Param("status") GoalOrhSubmissionStatus status
    );

    @Override
    @Query("""
            select submission
            from GdrGoalOrhSubmission submission
            join fetch submission.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch submission.submittedByUser submittedBy
            left join fetch submission.reviewedByUser reviewedBy
            where submission.id = :id
              and upper(submission.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrGoalOrhSubmission> findActiveByIdInActiveCycle(@Param("id") Long id);

    @Override
    @Query("""
            select submission
            from GdrGoalOrhSubmission submission
            join fetch submission.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch submission.submittedByUser submittedBy
            left join fetch submission.reviewedByUser reviewedBy
            where submission.status in (
                    pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus.ENVIADO,
                    pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus.REVISADO
                )
              and upper(submission.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by submission.submittedAt desc, submission.id desc
            """)
    List<GdrGoalOrhSubmission> findActiveReceptionItemsInActiveCycle();
}
