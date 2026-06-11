package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;

@Repository
public interface JpaGdrCasoCieRepository
        extends JpaRepository<GdrCasoCie, Long>, GdrCasoCieRepository {

    @Override
    @Query("""
            select c from GdrCasoCie c
            where c.solicitud.id = :solicitudId
            """)
    Optional<GdrCasoCie> findBySolicitudId(@Param("solicitudId") Long solicitudId);

    @Override
    @Query("""
            select c from GdrCasoCie c
            order by c.fechaIngresoCie desc
            """)
    List<GdrCasoCie> findAllOrderByFechaIngreso();

    @Override
    @Query("""
            select count(c) from GdrCasoCie c
            where c.solicitud.cycle.id = :cycleId
            """)
    long countByCycleId(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select count(c) from GdrCasoCie c
            where c.solicitud.cycle.id = :cycleId
              and c.estado = 'RECIBIDO'
            """)
    long countPendientesByCycleId(@Param("cycleId") Long cycleId);
}
