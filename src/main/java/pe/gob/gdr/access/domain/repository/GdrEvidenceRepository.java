package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvidence;

public interface GdrEvidenceRepository {

    List<GdrEvidence> findActiveForActiveCycle();

    List<GdrEvidence> findActiveByGoalIdInActiveCycle(Long goalId);

    Optional<GdrEvidence> findActiveByIdInActiveCycle(Long evidenceId);

    List<GdrEvidence> findActiveByGoalAssignmentIdInActiveCycle(Long assignmentId);

    long countActiveByGoalIdInActiveCycle(Long goalId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrEvidence> findActiveByCycle(Long cycleId);

    List<GdrEvidence> findActiveByGoalIdAndCycle(Long goalId, Long cycleId);

    Optional<GdrEvidence> findActiveByIdAndCycle(Long evidenceId, Long cycleId);

    List<GdrEvidence> findActiveByAssignmentIdAndCycle(Long assignmentId, Long cycleId);

    long countActiveByGoalIdAndCycle(Long goalId, Long cycleId);

    GdrEvidence save(GdrEvidence evidence);
}
