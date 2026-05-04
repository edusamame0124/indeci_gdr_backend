package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import pe.gob.gdr.access.domain.model.Role;

public interface RoleRepository {

    Optional<Role> findByCode(String code);

    List<Role> findActiveByCodes(Set<String> codes);
}
