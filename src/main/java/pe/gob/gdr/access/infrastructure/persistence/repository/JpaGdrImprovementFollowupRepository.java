package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrImprovementFollowup;
import pe.gob.gdr.access.domain.repository.GdrImprovementFollowupRepository;

@Repository
public interface JpaGdrImprovementFollowupRepository
        extends JpaRepository<GdrImprovementFollowup, Long>, GdrImprovementFollowupRepository {

    @Override
    @Query("""
            select followup
            from GdrImprovementFollowup followup
            where followup.opportunity.id = :opportunityId
            order by followup.registeredAt desc, followup.id desc
            """)
    List<GdrImprovementFollowup> findByOpportunityId(@Param("opportunityId") Long opportunityId);
}
