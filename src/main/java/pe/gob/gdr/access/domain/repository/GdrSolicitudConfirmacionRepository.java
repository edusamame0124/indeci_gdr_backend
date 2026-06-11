package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;

public interface GdrSolicitudConfirmacionRepository {

    Optional<GdrSolicitudConfirmacion> findById(Long id);

    Optional<GdrSolicitudConfirmacion> findByFinalEvaluationId(Long finalEvaluationId);

    List<GdrSolicitudConfirmacion> findByCycleIdOrderByFechaSolicitud(Long cycleId);

    GdrSolicitudConfirmacion save(GdrSolicitudConfirmacion solicitud);
}
