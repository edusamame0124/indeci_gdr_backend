package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.NotifCanal;

public interface NotifCanalRepository {

    Optional<NotifCanal> findActiveByCodigo(String codigoCanal);
}
