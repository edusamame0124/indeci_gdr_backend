package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.User;

public interface UserRepository {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameWithPerson(String username);

    User save(User user);
}
