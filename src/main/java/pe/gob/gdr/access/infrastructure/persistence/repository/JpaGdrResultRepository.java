package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

@Repository
public interface JpaGdrResultRepository extends JpaRepository<GdrResult, Long>, GdrResultRepository {

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch result.finalEvaluation evaluation
            where upper(result.status) = 'ACTIVE'
              and upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by result.updatedAt desc, result.id desc
            """)
    List<GdrResult> findAllInActiveCycle();

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch result.finalEvaluation evaluation
            where assignment.id = :assignmentId
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrResult> findByAssignmentIdInActiveCycle(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch result.finalEvaluation evaluation
            where evaluated.id = :evaluatedId
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrResult> findByEvaluatedPersonIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch result.finalEvaluation evaluation
            where assignment.cycle.id = :cycleId
              and upper(result.status) = 'ACTIVE'
              and upper(evaluation.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by result.updatedAt desc, result.id desc
            """)
    List<GdrResult> findAllByCycleId(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch result.finalEvaluation evaluation
            where assignment.id = :assignmentId
              and assignment.cycle.id = :cycleId
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            """)
    Optional<GdrResult> findByAssignmentIdAndCycle(
            @Param("assignmentId") Long assignmentId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select result
            from GdrResult result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch result.finalEvaluation evaluation
            where evaluated.id = :evaluatedId
              and assignment.cycle.id = :cycleId
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            """)
    Optional<GdrResult> findByEvaluatedPersonIdAndCycle(
            @Param("evaluatedId") Long evaluatedId,
            @Param("cycleId") Long cycleId
    );
}
