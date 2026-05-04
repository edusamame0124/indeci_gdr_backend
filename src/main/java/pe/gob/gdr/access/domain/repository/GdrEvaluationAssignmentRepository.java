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

    GdrEvaluationAssignment save(GdrEvaluationAssignment assignment);
}
