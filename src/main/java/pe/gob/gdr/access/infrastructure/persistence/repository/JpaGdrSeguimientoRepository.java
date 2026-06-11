package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;
import pe.gob.gdr.access.domain.repository.GdrSeguimientoRepository;

@Repository
public interface JpaGdrSeguimientoRepository
        extends JpaRepository<GdrSeguimiento, Long>, GdrSeguimientoRepository {

    @Override
    @Query("""
            select s from GdrSeguimiento s
            where s.assignment.id = :assignmentId
            order by s.fechaReunion asc
            """)
    List<GdrSeguimiento> findByAssignmentIdOrderByFechaReunion(@Param("assignmentId") Long assignmentId);

    @Override
    @Query("""
            select s from GdrSeguimiento s
            where s.cycle.id = :cycleId
            order by s.fechaReunion asc
            """)
    List<GdrSeguimiento> findByCycleIdOrderByFechaReunion(@Param("cycleId") Long cycleId);
}
