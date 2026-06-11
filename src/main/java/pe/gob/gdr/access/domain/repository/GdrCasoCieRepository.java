package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrCasoCie;

public interface GdrCasoCieRepository {

    Optional<GdrCasoCie> findById(Long id);

    Optional<GdrCasoCie> findBySolicitudId(Long solicitudId);

    List<GdrCasoCie> findAllOrderByFechaIngreso();

    long countByCycleId(Long cycleId);

    GdrCasoCie save(GdrCasoCie caso);

    /** Cuenta los casos CIE del ciclo que aún están en estado RECIBIDO (pendientes de resolución). */
    long countPendientesByCycleId(Long cycleId);
}
