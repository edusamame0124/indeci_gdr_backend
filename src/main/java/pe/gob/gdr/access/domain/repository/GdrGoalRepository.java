package pe.gob.gdr.access.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrGoal;

public interface GdrGoalRepository {

    List<GdrGoal> findActiveGoalsForActiveCycle();

    Optional<GdrGoal> findActiveByIdInActiveCycle(Long goalId);

    List<GdrGoal> findActiveGoalsByAssignmentIdInActiveCycle(Long assignmentId);

    List<GdrGoal> findActiveGoalsByPersonIdInActiveCycle(Long personId);

    BigDecimal sumWeightByAssignmentExcludingGoal(Long assignmentId, Long excludedGoalId);

    /**
     * VAL-07 — Retorna los nombres de evaluados cuyas metas activas en el ciclo
     * no suman exactamente 100% de peso. Usado al cerrar la etapa de planificación.
     */
    List<String> findEvaluadosConPesoIncorrectoEnCiclo(Long cycleId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrGoal> findActiveGoalsByCycle(Long cycleId);

    Optional<GdrGoal> findActiveByIdAndCycle(Long goalId, Long cycleId);

    List<GdrGoal> findActiveGoalsByAssignmentIdAndCycle(Long assignmentId, Long cycleId);

    List<GdrGoal> findActiveGoalsByPersonIdAndCycle(Long personId, Long cycleId);

    GdrGoal save(GdrGoal goal);
}
