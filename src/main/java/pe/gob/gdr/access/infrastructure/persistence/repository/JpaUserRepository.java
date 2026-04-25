package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            left join fetch u.person person
            left join fetch person.orgUnit orgUnit
            where lower(u.username) = lower(:loginId)
               or lower(u.email) = lower(:loginId)
            """)
    Optional<User> findByLoginId(@Param("loginId") String loginId);

    @Override
    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            left join fetch u.person person
            left join fetch person.orgUnit orgUnit
            where lower(u.username) = lower(:username)
            """)
    Optional<User> findByUsername(@Param("username") String username);

    @Override
    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            left join fetch u.person person
            left join fetch person.orgUnit orgUnit
            where lower(u.username) = lower(:username)
            """)
    Optional<User> findByUsernameWithPerson(@Param("username") String username);
}
