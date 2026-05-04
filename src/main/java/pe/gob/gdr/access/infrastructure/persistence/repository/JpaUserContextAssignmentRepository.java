package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.UserContextAssignment;
import pe.gob.gdr.access.domain.repository.UserContextAssignmentRepository;

@Repository
public interface JpaUserContextAssignmentRepository
        extends JpaRepository<UserContextAssignment, Long>, UserContextAssignmentRepository {

    @Override
    @Query("""
            select uca
            from UserContextAssignment uca
            join fetch uca.user u
            join fetch uca.cycle c
            where lower(u.username) = lower(:username)
              and c.id = :cycleId
              and upper(uca.status) = 'ACTIVE'
            """)
    Optional<UserContextAssignment> findActiveByUsernameAndCycleId(
            @Param("username") String username,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query("""
            select uca
            from UserContextAssignment uca
            join fetch uca.user u
            join fetch uca.cycle c
            where u.id = :userId
              and c.id = :cycleId
            """)
    Optional<UserContextAssignment> findByUserIdAndCycleId(
            @Param("userId") Long userId,
            @Param("cycleId") Long cycleId
    );
}
