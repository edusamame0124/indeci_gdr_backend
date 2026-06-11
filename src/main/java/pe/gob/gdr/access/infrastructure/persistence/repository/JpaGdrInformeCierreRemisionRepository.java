package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrInformeCierreRemision;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRemisionRepository;

@Repository
public interface JpaGdrInformeCierreRemisionRepository
        extends JpaRepository<GdrInformeCierreRemision, Long>, GdrInformeCierreRemisionRepository {

    @Override
    @Query("""
            select r from GdrInformeCierreRemision r
            where r.informeCierre.id = :informeId
            order by r.fechaRemision desc
            """)
    List<GdrInformeCierreRemision> findByInformeCierreIdOrderByFechaRemisionDesc(
            @Param("informeId") Long informeId);
}
