package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.Role;
import pe.gob.gdr.access.domain.repository.RoleRepository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {

    @Override
    Optional<Role> findByCode(String code);
}
