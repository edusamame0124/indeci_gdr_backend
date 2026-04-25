package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;

@Repository
public interface JpaGdrEvidenceRepository extends JpaRepository<GdrEvidence, Long>, GdrEvidenceRepository {

    @Override
    @Query("""
            select evidence
            from GdrEvidence evidence
            join fetch evidence.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evidence.evidenceStatus evidenceStatus
            where upper(evidence.status) = 'ACTIVE'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evidence.updatedAt desc, evidence.id desc
            """)
    List<GdrEvidence> findActiveForActiveCycle();

    @Override
    @Query("""
            select evidence
            from GdrEvidence evidence
            join fetch evidence.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evidence.evidenceStatus evidenceStatus
            where goal.id = :goalId
              and upper(evidence.status) = 'ACTIVE'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evidence.updatedAt desc, evidence.id desc
            """)
    List<GdrEvidence> findActiveByGoalIdInActiveCycle(@Param("goalId") Long goalId);

    @Override
    @Query("""
            select evidence
            from GdrEvidence evidence
            join fetch evidence.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evidence.evidenceStatus evidenceStatus
            where evidence.id = :evidenceId
              and upper(evidence.status) = 'ACTIVE'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrEvidence> findActiveByIdInActiveCycle(@Param("evidenceId") Long evidenceId);

    @Override
    @Query("""
            select evidence
            from GdrEvidence evidence
            join fetch evidence.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evidence.evidenceStatus evidenceStatus
            where assignment.id = :assignmentId
              and upper(evidence.status) = 'ACTIVE'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evidence.updatedAt desc, evidence.id desc
            """)
    List<GdrEvidence> findActiveByGoalAssignmentIdInActiveCycle(@Param("assignmentId") Long assignmentId);
}
