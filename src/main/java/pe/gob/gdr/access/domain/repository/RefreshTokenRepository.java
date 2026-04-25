package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.RefreshToken;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findActiveByTokenValue(String tokenValue);

    RefreshToken save(RefreshToken refreshToken);
}
