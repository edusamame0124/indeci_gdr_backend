package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.NotifCanal;
import pe.gob.gdr.access.domain.repository.NotifCanalRepository;

@Repository
public interface JpaNotifCanalRepository extends JpaRepository<NotifCanal, Long>, NotifCanalRepository {

    @Override
    @Query("""
            select canal
            from NotifCanal canal
            where upper(canal.estadoRegistro) = 'ACTIVO'
              and upper(canal.codigoCanal) = upper(:codigoCanal)
            """)
    Optional<NotifCanal> findActiveByCodigo(@Param("codigoCanal") String codigoCanal);
}
