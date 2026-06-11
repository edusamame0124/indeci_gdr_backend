package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.User;

public interface UserRepository {

    List<User> findAllForAdministration();

    Optional<User> findByIdForAdministration(Long id);

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameWithPerson(String username);

    List<User> findActiveGdrUsersByPersonId(Long personId);

    List<String> findActiveUsernamesByRoleCode(String roleCode);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailForAnotherUser(String email, Long userId);

    boolean existsByPersonIdForAnotherUser(Long personId, Long userId);

    User save(User user);
}
