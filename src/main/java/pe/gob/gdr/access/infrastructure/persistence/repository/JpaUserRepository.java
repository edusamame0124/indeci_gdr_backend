package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
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
            order by lower(u.username)
            """)
    List<User> findAllForAdministration();

    @Override
    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            left join fetch u.person person
            left join fetch person.orgUnit orgUnit
            where u.id = :id
            """)
    Optional<User> findByIdForAdministration(@Param("id") Long id);

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

    @Override
    @Query("""
            select distinct u
            from User u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            left join fetch u.person person
            left join fetch person.orgUnit orgUnit
            where person.id = :personId
              and upper(u.status) = 'ACTIVE'
              and exists (
                  select 1
                  from UserRole activeUr
                  join activeUr.role activeRole
                  where activeUr.user.id = u.id
                    and upper(activeUr.status) = 'ACTIVE'
                    and upper(activeRole.status) = 'ACTIVE'
                    and upper(activeRole.code) = 'GDR_USUARIO'
              )
            """)
    List<User> findActiveGdrUsersByPersonId(@Param("personId") Long personId);

    @Override
    @Query("""
            select count(u) > 0
            from User u
            where lower(u.username) = lower(:username)
            """)
    boolean existsByUsername(@Param("username") String username);

    @Override
    @Query("""
            select count(u) > 0
            from User u
            where lower(u.email) = lower(:email)
            """)
    boolean existsByEmail(@Param("email") String email);

    @Override
    @Query("""
            select count(u) > 0
            from User u
            where lower(u.email) = lower(:email)
              and u.id <> :userId
            """)
    boolean existsByEmailForAnotherUser(@Param("email") String email, @Param("userId") Long userId);

    @Override
    @Query("""
            select count(u) > 0
            from User u
            where u.person.id = :personId
              and u.id <> :userId
            """)
    boolean existsByPersonIdForAnotherUser(@Param("personId") Long personId, @Param("userId") Long userId);
}
