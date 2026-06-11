package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrCieConformacion;

public interface GdrCieConformacionRepository {

    GdrCieConformacion save(GdrCieConformacion conformacion);

    Optional<GdrCieConformacion> findById(Long id);

    List<GdrCieConformacion> findAllOrderByVigenciaInicioDesc();

    List<GdrCieConformacion> findByCycleIdOrderByVigenciaInicioDesc(Long cycleId);

    List<GdrCieConformacion> findInstitucionalesOrderByVigenciaInicioDesc();

    boolean existsConformacionVigenteParaCiclo(Long cycleId);
}
