package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.Role;

public interface RoleRepository {

    Optional<Role> findByCode(String code);
}
