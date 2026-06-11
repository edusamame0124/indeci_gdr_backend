package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;

@Repository
public interface JpaGdrImprovementOpportunityRepository
        extends JpaRepository<GdrImprovementOpportunity, Long>, GdrImprovementOpportunityRepository {

    @Override
    @Query("""
            select opportunity
            from GdrImprovementOpportunity opportunity
            join fetch opportunity.improvementStatus status
            join fetch opportunity.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where upper(opportunity.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by opportunity.createdAt desc
            """)
    List<GdrImprovementOpportunity> findAllInActiveCycle();

    @Override
    @Query("""
            select opportunity
            from GdrImprovementOpportunity opportunity
            join fetch opportunity.improvementStatus status
            join fetch opportunity.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where evaluated.id = :evaluatedId
              and upper(opportunity.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by opportunity.createdAt desc
            """)
    List<GdrImprovementOpportunity> findActiveByEvaluatedIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);

    @Override
    @Query("""
            select opportunity
            from GdrImprovementOpportunity opportunity
            join fetch opportunity.improvementStatus status
            join fetch opportunity.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where opportunity.id = :opportunityId
              and upper(opportunity.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrImprovementOpportunity> findActiveById(@Param("opportunityId") Long opportunityId);

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

    @Override
    @Query("""
            select opportunity
            from GdrImprovementOpportunity opportunity
            join fetch opportunity.improvementStatus status
            join fetch opportunity.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where result.assignment.cycle.id = :cycleId
              and upper(opportunity.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by opportunity.createdAt desc
            """)
    List<GdrImprovementOpportunity> findAllByCycleId(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select opportunity
            from GdrImprovementOpportunity opportunity
            join fetch opportunity.improvementStatus status
            join fetch opportunity.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where evaluated.id = :evaluatedId
              and result.assignment.cycle.id = :cycleId
              and upper(opportunity.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by opportunity.createdAt desc
            """)
    List<GdrImprovementOpportunity> findActiveByEvaluatedIdAndCycle(
            @Param("evaluatedId") Long evaluatedId,
            @Param("cycleId") Long cycleId
    );
}
