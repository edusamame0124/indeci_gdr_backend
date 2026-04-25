package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.RefreshToken;
import pe.gob.gdr.access.domain.repository.RefreshTokenRepository;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

    @Override
    @Query("""
            select rt
            from RefreshToken rt
            join fetch rt.user u
            left join fetch u.userRoles ur
            left join fetch ur.role r
            where rt.tokenValue = :tokenValue
              and rt.revokedAt is null
            """)
    Optional<RefreshToken> findActiveByTokenValue(@Param("tokenValue") String tokenValue);
}
