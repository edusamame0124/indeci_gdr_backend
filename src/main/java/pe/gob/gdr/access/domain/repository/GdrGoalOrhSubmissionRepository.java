package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrGoalOrhSubmission;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;

public interface GdrGoalOrhSubmissionRepository {

    boolean existsActiveSubmission(Long goalId, GoalOrhSubmissionStatus status);

    Optional<GdrGoalOrhSubmission> findActiveByIdInActiveCycle(Long id);

    List<GdrGoalOrhSubmission> findActiveReceptionItemsInActiveCycle();

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    Optional<GdrGoalOrhSubmission> findActiveByIdAndCycle(Long id, Long cycleId);

    List<GdrGoalOrhSubmission> findActiveReceptionItemsByCycle(Long cycleId);

    GdrGoalOrhSubmission save(GdrGoalOrhSubmission submission);
}
