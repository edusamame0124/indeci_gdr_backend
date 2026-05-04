package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.UserContextAssignment;

public interface UserContextAssignmentRepository {

    Optional<UserContextAssignment> findActiveByUsernameAndCycleId(String username, Long cycleId);

    Optional<UserContextAssignment> findByUserIdAndCycleId(Long userId, Long cycleId);

    UserContextAssignment save(UserContextAssignment assignment);
}
