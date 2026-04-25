package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.NotifPlantilla;
import pe.gob.gdr.access.domain.repository.NotifPlantillaRepository;

@Repository
public interface JpaNotifPlantillaRepository extends JpaRepository<NotifPlantilla, Long>, NotifPlantillaRepository {

    @Override
    @Query("""
            select plantilla
            from NotifPlantilla plantilla
            join fetch plantilla.canal canal
            where upper(plantilla.estadoRegistro) = 'ACTIVO'
              and upper(canal.estadoRegistro) = 'ACTIVO'
              and upper(plantilla.codigoPlantilla) = upper(:codigoPlantilla)
            """)
    Optional<NotifPlantilla> findActiveByCodigo(@Param("codigoPlantilla") String codigoPlantilla);
}
