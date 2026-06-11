package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;

@Repository
public interface JpaGdrEvaluationAssignmentRepository
        extends JpaRepository<GdrEvaluationAssignment, Long>, GdrEvaluationAssignmentRepository {

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evaluated.displayName asc, evaluator.displayName asc
            """)
    List<GdrEvaluationAssignment> findActiveAssignmentsForActiveCycle();

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where evaluated.id = :evaluatedId
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByEvaluatedIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where (evaluator.id = :personId or evaluated.id = :personId)
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByPersonIdInActiveCycle(@Param("personId") Long personId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where assignment.id = :assignmentId
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrEvaluationAssignment> findActiveByIdInActiveCycle(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            where cycle.id = :cycleId
            order by evaluated.displayName asc, evaluator.displayName asc, assignment.id asc
            """)
    List<GdrEvaluationAssignment> findByCycleIdForAdministration(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where assignment.id = :assignmentId
            """)
    Optional<GdrEvaluationAssignment> findByIdForAdministration(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select case when count(assignment) > 0 then true else false end
            from GdrEvaluationAssignment assignment
            where assignment.cycle.id = :cycleId
              and assignment.evaluatorPerson.id = :evaluatorPersonId
              and assignment.evaluatedPerson.id = :evaluatedPersonId
              and upper(assignment.status) = 'ACTIVE'
            """)
    boolean existsActivePairInCycle(
            @Param("cycleId") Long cycleId,
            @Param("evaluatorPersonId") Long evaluatorPersonId,
            @Param("evaluatedPersonId") Long evaluatedPersonId
    );

    @Override
    @Query("""
            select case when count(assignment) > 0 then true else false end
            from GdrEvaluationAssignment assignment
            where assignment.cycle.id = :cycleId
              and assignment.evaluatorPerson.id = :evaluatorPersonId
              and assignment.evaluatedPerson.id = :evaluatedPersonId
              and upper(assignment.status) = 'ACTIVE'
              and assignment.id <> :excludedAssignmentId
            """)
    boolean existsActivePairInCycleExcludingId(
            @Param("cycleId") Long cycleId,
            @Param("evaluatorPersonId") Long evaluatorPersonId,
            @Param("evaluatedPersonId") Long evaluatedPersonId,
            @Param("excludedAssignmentId") Long excludedAssignmentId
    );

    @Override
    @Query("""
            select case when count(goal) > 0 then true else false end
            from GdrGoal goal
            where goal.assignment.id = :assignmentId
              and upper(goal.status) = 'ACTIVE'
            """)
    boolean hasActiveGoals(@Param("assignmentId") Long assignmentId);

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where assignment.cycle.id = :cycleId
              and upper(assignment.status) = 'ACTIVE'
            order by evaluated.displayName asc, evaluator.displayName asc
            """)
    List<GdrEvaluationAssignment> findActiveAssignmentsByCycle(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where assignment.id = :assignmentId
              and assignment.cycle.id = :cycleId
              and upper(assignment.status) = 'ACTIVE'
            """)
    Optional<GdrEvaluationAssignment> findActiveByIdAndCycle(
            @Param("assignmentId") Long assignmentId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where evaluated.id = :evaluatedId
              and assignment.cycle.id = :cycleId
              and upper(assignment.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByEvaluatedIdAndCycle(
            @Param("evaluatedId") Long evaluatedId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch assignment.segment seg
            where (evaluator.id = :personId or evaluated.id = :personId)
              and assignment.cycle.id = :cycleId
              and upper(assignment.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByPersonIdAndCycle(
            @Param("personId") Long personId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select a.evaluatedPerson.displayName
            from GdrEvaluationAssignment a
            where a.cycle.id = :cycleId
              and upper(a.status) = 'ACTIVE'
              and not exists (
                select 1 from GdrFinalEvaluation e
                where e.assignment.id = a.id
                  and upper(e.status) = 'ACTIVE'
              )
            order by a.evaluatedPerson.displayName asc
            """)
    List<String> findNombresSinEvaluacionFinalEnCiclo(@Param("cycleId") Long cycleId);
}
