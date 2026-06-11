package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;

public interface GdrEvaluationAssignmentRepository {

    List<GdrEvaluationAssignment> findActiveAssignmentsForActiveCycle();

    List<GdrEvaluationAssignment> findActiveByEvaluatedIdInActiveCycle(Long evaluatedId);

    List<GdrEvaluationAssignment> findActiveByPersonIdInActiveCycle(Long personId);

    Optional<GdrEvaluationAssignment> findActiveByIdInActiveCycle(Long assignmentId);

    List<GdrEvaluationAssignment> findByCycleIdForAdministration(Long cycleId);

    Optional<GdrEvaluationAssignment> findByIdForAdministration(Long assignmentId);

    boolean existsActivePairInCycle(Long cycleId, Long evaluatorPersonId, Long evaluatedPersonId);

    boolean existsActivePairInCycleExcludingId(
            Long cycleId,
            Long evaluatorPersonId,
            Long evaluatedPersonId,
            Long excludedAssignmentId
    );

    boolean hasActiveGoals(Long assignmentId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrEvaluationAssignment> findActiveAssignmentsByCycle(Long cycleId);

    Optional<GdrEvaluationAssignment> findActiveByIdAndCycle(Long assignmentId, Long cycleId);

    List<GdrEvaluationAssignment> findActiveByEvaluatedIdAndCycle(Long evaluatedId, Long cycleId);

    List<GdrEvaluationAssignment> findActiveByPersonIdAndCycle(Long personId, Long cycleId);

    GdrEvaluationAssignment save(GdrEvaluationAssignment assignment);

    /** Retorna los nombres de evaluados activos en el ciclo que no tienen evaluación final registrada. */
    List<String> findNombresSinEvaluacionFinalEnCiclo(Long cycleId);
}
