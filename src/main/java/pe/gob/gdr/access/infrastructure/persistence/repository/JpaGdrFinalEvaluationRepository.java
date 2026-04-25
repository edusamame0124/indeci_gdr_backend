package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;

@Repository
public interface JpaGdrFinalEvaluationRepository
        extends JpaRepository<GdrFinalEvaluation, Long>, GdrFinalEvaluationRepository {

    @Override
    @Query("""
            select evaluation
            from GdrFinalEvaluation evaluation
            join fetch evaluation.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            where upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evaluated.displayName asc
            """)
    List<GdrFinalEvaluation> findActiveFinalEvaluationsForActiveCycle();

    @Override
    @Query("""
            select evaluation
            from GdrFinalEvaluation evaluation
            join fetch evaluation.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            where assignment.id = :assignmentId
              and upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrFinalEvaluation> findByAssignmentIdInActiveCycle(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select evaluation
            from GdrFinalEvaluation evaluation
            join fetch evaluation.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            where evaluation.id = :evaluationId
              and upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrFinalEvaluation> findByIdInActiveCycle(@Param("evaluationId") Long evaluationId);

    @Override
    @Query("""
            select evaluation
            from GdrFinalEvaluation evaluation
            join fetch evaluation.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            where evaluated.id = :evaluatedId
              and upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrFinalEvaluation> findByEvaluatedPersonIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);
}

