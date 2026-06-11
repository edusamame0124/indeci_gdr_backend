package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;

public interface GdrFinalEvaluationRepository {

    List<GdrFinalEvaluation> findActiveFinalEvaluationsForActiveCycle();

    Optional<GdrFinalEvaluation> findByAssignmentIdInActiveCycle(Long assignmentId);

    Optional<GdrFinalEvaluation> findByIdInActiveCycle(Long evaluationId);

    Optional<GdrFinalEvaluation> findByEvaluatedPersonIdInActiveCycle(Long evaluatedId);

    /**
     * VAL-13A — Cuenta evaluaciones finales activas en el ciclo sin retroalimentación
     * final registrada (fechaReunionRetroFinal IS NULL = evaluado no notificado formalmente).
     */
    int countSinNotificarEnCiclo(Long cycleId);

    /**
     * VAL-13A — Retorna los nombres de evaluados sin retroalimentación final registrada.
     */
    List<String> findNombresSinNotificarEnCiclo(Long cycleId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrFinalEvaluation> findActiveFinalEvaluationsByCycle(Long cycleId);

    Optional<GdrFinalEvaluation> findByAssignmentIdAndCycle(Long assignmentId, Long cycleId);

    Optional<GdrFinalEvaluation> findByIdAndCycle(Long evaluationId, Long cycleId);

    Optional<GdrFinalEvaluation> findByEvaluatedPersonIdAndCycle(Long evaluatedId, Long cycleId);

    GdrFinalEvaluation save(GdrFinalEvaluation evaluation);
}

