package pe.gob.gdr.access.domain.repository;

import java.util.List;
import pe.gob.gdr.access.domain.model.GdrImprovementFollowup;

public interface GdrImprovementFollowupRepository {

    GdrImprovementFollowup save(GdrImprovementFollowup followup);

    List<GdrImprovementFollowup> findByOpportunityId(Long opportunityId);
}
