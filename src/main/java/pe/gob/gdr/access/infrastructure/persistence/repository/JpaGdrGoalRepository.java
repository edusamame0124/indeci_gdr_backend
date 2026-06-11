package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;

@Repository
public interface JpaGdrGoalRepository extends JpaRepository<GdrGoal, Long>, GdrGoalRepository {

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsForActiveCycle();

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where goal.id = :goalId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrGoal> findActiveByIdInActiveCycle(@Param("goalId") Long goalId);

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where assignment.id = :assignmentId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsByAssignmentIdInActiveCycle(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where (evaluator.id = :personId or evaluated.id = :personId)
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsByPersonIdInActiveCycle(@Param("personId") Long personId);

    @Override
    @Query("""
            select coalesce(sum(goal.weight), 0)
            from GdrGoal goal
            where goal.assignment.id = :assignmentId
              and upper(goal.status) = 'ACTIVE'
              and (:excludedGoalId is null or goal.id <> :excludedGoalId)
            """)
    BigDecimal sumWeightByAssignmentExcludingGoal(
            @Param("assignmentId") Long assignmentId,
            @Param("excludedGoalId") Long excludedGoalId
    );

    @Override
    @Query("""
            select evaluated.displayName
            from GdrGoal goal
            join goal.assignment assignment
            join assignment.cycle cycle
            join assignment.evaluatedPerson evaluated
            where cycle.id = :cycleId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            group by assignment.id, evaluated.displayName
            having sum(goal.weight) < 99.995 or sum(goal.weight) > 100.005
            order by evaluated.displayName asc
            """)
    List<String> findEvaluadosConPesoIncorrectoEnCiclo(@Param("cycleId") Long cycleId);

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where assignment.cycle.id = :cycleId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsByCycle(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where goal.id = :goalId
              and assignment.cycle.id = :cycleId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            """)
    Optional<GdrGoal> findActiveByIdAndCycle(@Param("goalId") Long goalId, @Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where assignment.id = :assignmentId
              and assignment.cycle.id = :cycleId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsByAssignmentIdAndCycle(
            @Param("assignmentId") Long assignmentId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select goal
            from GdrGoal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            join fetch goal.indicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where (evaluator.id = :personId or evaluated.id = :personId)
              and assignment.cycle.id = :cycleId
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by goal.updatedAt desc, goal.id desc
            """)
    List<GdrGoal> findActiveGoalsByPersonIdAndCycle(
            @Param("personId") Long personId,
            @Param("cycleId") Long cycleId
    );
}
