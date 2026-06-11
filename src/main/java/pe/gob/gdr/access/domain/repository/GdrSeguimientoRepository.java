package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;

public interface GdrSeguimientoRepository {

    List<GdrSeguimiento> findByAssignmentIdOrderByFechaReunion(Long assignmentId);

    List<GdrSeguimiento> findByCycleIdOrderByFechaReunion(Long cycleId);

    Optional<GdrSeguimiento> findById(Long id);

    GdrSeguimiento save(GdrSeguimiento seguimiento);

    void deleteById(Long id);
}
