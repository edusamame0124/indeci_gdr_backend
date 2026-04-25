package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.NotifPlantilla;

public interface NotifPlantillaRepository {

    Optional<NotifPlantilla> findActiveByCodigo(String codigoPlantilla);
}
