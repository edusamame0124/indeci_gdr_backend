package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrCronograma;
import pe.gob.gdr.access.domain.repository.GdrCronogramaRepository;

@Repository
public interface JpaGdrCronogramaRepository
        extends JpaRepository<GdrCronograma, Long>, GdrCronogramaRepository {

    @Override
    @Query("""
            select c from GdrCronograma c
            where c.cycle.id = :cycleId
            order by c.fechaInicio asc
            """)
    List<GdrCronograma> findByCycleIdOrderByFechaInicio(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select c from GdrCronograma c
            where c.cycle.id = :cycleId and c.etapa = :etapa
            """)
    Optional<GdrCronograma> findByCycleIdAndEtapa(
            @Param("cycleId") Long cycleId,
            @Param("etapa") String etapa);
}
