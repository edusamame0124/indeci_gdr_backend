package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.ConsentRegistro;

public interface ConsentRegistroRepository {

    List<ConsentRegistro> findActiveByUsername(String username);

    Optional<ConsentRegistro> findActiveByUsernameAndTypeVersion(String username, Long consentTypeId, Integer versionConsentimiento);

    ConsentRegistro save(ConsentRegistro registro);
}
