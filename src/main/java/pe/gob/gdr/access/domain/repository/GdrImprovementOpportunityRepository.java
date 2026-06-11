package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;

public interface GdrImprovementOpportunityRepository {

    List<GdrImprovementOpportunity> findAllInActiveCycle();

    List<GdrImprovementOpportunity> findActiveByEvaluatedIdInActiveCycle(Long evaluatedId);

    Optional<GdrImprovementOpportunity> findActiveById(Long opportunityId);

    // ── Métodos cycle-aware (P2) ──────────────────────────────────────────────

    List<GdrImprovementOpportunity> findAllByCycleId(Long cycleId);

    List<GdrImprovementOpportunity> findActiveByEvaluatedIdAndCycle(Long evaluatedId, Long cycleId);

    GdrImprovementOpportunity save(GdrImprovementOpportunity opportunity);
}
