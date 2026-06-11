package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;

@Repository
public interface JpaGdrSolicitudConfirmacionRepository
        extends JpaRepository<GdrSolicitudConfirmacion, Long>, GdrSolicitudConfirmacionRepository {

    @Override
    @Query("""
            select s from GdrSolicitudConfirmacion s
            where s.finalEvaluation.id = :finalEvaluationId
            """)
    Optional<GdrSolicitudConfirmacion> findByFinalEvaluationId(@Param("finalEvaluationId") Long finalEvaluationId);

    @Override
    @Query("""
            select s from GdrSolicitudConfirmacion s
            where s.cycle.id = :cycleId
            order by s.fechaSolicitud desc
            """)
    List<GdrSolicitudConfirmacion> findByCycleIdOrderByFechaSolicitud(@Param("cycleId") Long cycleId);
}
