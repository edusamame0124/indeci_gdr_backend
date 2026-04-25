package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.ConsentTipo;
import pe.gob.gdr.access.domain.repository.ConsentTipoRepository;

@Repository
public interface JpaConsentTipoRepository extends JpaRepository<ConsentTipo, Long>, ConsentTipoRepository {

    @Override
    @Query("""
            select tipo
            from ConsentTipo tipo
            where upper(tipo.estadoRegistro) = 'ACTIVO'
            order by tipo.codigoConsentimiento asc, tipo.versionConsentimiento desc
            """)
    List<ConsentTipo> findActiveTypes();

    @Override
    @Query("""
            select tipo
            from ConsentTipo tipo
            where tipo.id = :consentTypeId
              and upper(tipo.estadoRegistro) = 'ACTIVO'
            """)
    Optional<ConsentTipo> findActiveById(@Param("consentTypeId") Long consentTypeId);
}
