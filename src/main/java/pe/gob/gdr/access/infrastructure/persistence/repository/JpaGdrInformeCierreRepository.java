package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrInformeCierre;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRepository;

@Repository
public interface JpaGdrInformeCierreRepository
        extends JpaRepository<GdrInformeCierre, Long>, GdrInformeCierreRepository {

    @Override
    @Query("""
            select i from GdrInformeCierre i
            where i.cycle.id = :cycleId
            order by i.fechaGeneracion desc
            """)
    List<GdrInformeCierre> findByCycleIdOrderByFechaGeneracionDesc(@Param("cycleId") Long cycleId);

    @Override
    default Optional<GdrInformeCierre> findLatestByCycleId(Long cycleId) {
        return findByCycleIdOrderByFechaGeneracionDesc(cycleId).stream().findFirst();
    }
}
