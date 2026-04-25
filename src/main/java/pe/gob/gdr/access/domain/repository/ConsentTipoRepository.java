package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.ConsentTipo;

public interface ConsentTipoRepository {

    List<ConsentTipo> findActiveTypes();

    Optional<ConsentTipo> findActiveById(Long consentTypeId);
}
