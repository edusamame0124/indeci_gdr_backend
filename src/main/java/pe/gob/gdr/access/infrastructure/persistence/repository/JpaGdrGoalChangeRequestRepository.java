package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrGoalChangeRequest;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.GoalChangeRequestType;
import pe.gob.gdr.access.domain.repository.GdrGoalChangeRequestRepository;

@Repository
public interface JpaGdrGoalChangeRequestRepository
        extends JpaRepository<GdrGoalChangeRequest, Long>, GdrGoalChangeRequestRepository {

    @Override
    @Query("""
            select count(request) > 0
            from GdrGoalChangeRequest request
            where request.goal.id = :goalId
              and request.requestedByUser.id = :requestedByUserId
              and request.requestType = :requestType
              and request.status = :status
              and upper(request.recordStatus) = 'ACTIVO'
            """)
    boolean existsActiveRequest(
            @Param("goalId") Long goalId,
            @Param("requestedByUserId") Long requestedByUserId,
            @Param("requestType") GoalChangeRequestType requestType,
            @Param("status") GoalChangeRequestStatus status
    );

    @Override
    @Query("""
            select request
            from GdrGoalChangeRequest request
            join fetch request.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch request.requestedByUser requestedBy
            left join fetch request.reviewedByUser reviewedBy
            where request.id = :id
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrGoalChangeRequest> findActiveByIdInActiveCycle(@Param("id") Long id);

    @Override
    @Query("""
            select request
            from GdrGoalChangeRequest request
            join fetch request.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch request.requestedByUser requestedBy
            left join fetch request.reviewedByUser reviewedBy
            where request.status in (
                    pe.gob.gdr.access.domain.model.GoalChangeRequestStatus.PENDIENTE,
                    pe.gob.gdr.access.domain.model.GoalChangeRequestStatus.REVISADO
                )
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by request.createdAt desc, request.id desc
            """)
    List<GdrGoalChangeRequest> findActiveReceptionItemsInActiveCycle();

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

    @Override
    @Query("""
            select request
            from GdrGoalChangeRequest request
            join fetch request.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch request.requestedByUser requestedBy
            left join fetch request.reviewedByUser reviewedBy
            where request.id = :id
              and cycle.id = :cycleId
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            """)
    Optional<GdrGoalChangeRequest> findActiveByIdAndCycle(@Param("id") Long id, @Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select request
            from GdrGoalChangeRequest request
            join fetch request.goal goal
            join fetch goal.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch goal.indicator indicator
            join fetch request.requestedByUser requestedBy
            left join fetch request.reviewedByUser reviewedBy
            where cycle.id = :cycleId
              and request.status in (
                    pe.gob.gdr.access.domain.model.GoalChangeRequestStatus.PENDIENTE,
                    pe.gob.gdr.access.domain.model.GoalChangeRequestStatus.REVISADO
                )
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(goal.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by request.createdAt desc, request.id desc
            """)
    List<GdrGoalChangeRequest> findActiveReceptionItemsByCycle(@Param("cycleId") Long cycleId);
}
