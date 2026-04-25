package pe.gob.gdr.access.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrGoal;

public interface GdrGoalRepository {

    List<GdrGoal> findActiveGoalsForActiveCycle();

    Optional<GdrGoal> findActiveByIdInActiveCycle(Long goalId);

    List<GdrGoal> findActiveGoalsByAssignmentIdInActiveCycle(Long assignmentId);

    List<GdrGoal> findActiveGoalsByPersonIdInActiveCycle(Long personId);

    BigDecimal sumWeightByAssignmentExcludingGoal(Long assignmentId, Long excludedGoalId);

    GdrGoal save(GdrGoal goal);
}
