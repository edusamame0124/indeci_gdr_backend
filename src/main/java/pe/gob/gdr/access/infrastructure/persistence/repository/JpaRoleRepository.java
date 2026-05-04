package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.Role;
import pe.gob.gdr.access.domain.repository.RoleRepository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {

    @Override
    Optional<Role> findByCode(String code);

    @Override
    @Query("""
            select r
            from Role r
            where r.code in :codes
              and upper(r.status) = 'ACTIVE'
            order by r.code
            """)
    List<Role> findActiveByCodes(@Param("codes") Set<String> codes);
}
