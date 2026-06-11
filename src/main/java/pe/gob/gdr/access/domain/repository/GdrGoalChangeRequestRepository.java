package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrGoalChangeRequest;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.GoalChangeRequestType;

public interface GdrGoalChangeRequestRepository {

    boolean existsActiveRequest(
            Long goalId,
            Long requestedByUserId,
            GoalChangeRequestType requestType,
            GoalChangeRequestStatus status
    );

    Optional<GdrGoalChangeRequest> findActiveByIdInActiveCycle(Long id);

    List<GdrGoalChangeRequest> findActiveReceptionItemsInActiveCycle();

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    Optional<GdrGoalChangeRequest> findActiveByIdAndCycle(Long id, Long cycleId);

    List<GdrGoalChangeRequest> findActiveReceptionItemsByCycle(Long cycleId);

    GdrGoalChangeRequest save(GdrGoalChangeRequest request);
}
