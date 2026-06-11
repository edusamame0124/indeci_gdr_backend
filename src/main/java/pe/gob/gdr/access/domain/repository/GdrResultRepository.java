package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrResult;

public interface GdrResultRepository {

    List<GdrResult> findAllInActiveCycle();

    Optional<GdrResult> findByAssignmentIdInActiveCycle(Long assignmentId);

    Optional<GdrResult> findByEvaluatedPersonIdInActiveCycle(Long evaluatedId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrResult> findAllByCycleId(Long cycleId);

    Optional<GdrResult> findByAssignmentIdAndCycle(Long assignmentId, Long cycleId);

    Optional<GdrResult> findByEvaluatedPersonIdAndCycle(Long evaluatedId, Long cycleId);

    GdrResult save(GdrResult result);
}
