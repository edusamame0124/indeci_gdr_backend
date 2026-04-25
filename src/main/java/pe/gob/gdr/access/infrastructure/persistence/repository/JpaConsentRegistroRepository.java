package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.ConsentRegistro;
import pe.gob.gdr.access.domain.repository.ConsentRegistroRepository;

@Repository
public interface JpaConsentRegistroRepository extends JpaRepository<ConsentRegistro, Long>, ConsentRegistroRepository {

    @Override
    @Query("""
            select registro
            from ConsentRegistro registro
            join fetch registro.usuario usuario
            join fetch registro.tipoConsentimiento tipo
            where upper(registro.estadoRegistro) = 'ACTIVO'
              and upper(usuario.status) = 'ACTIVE'
              and upper(tipo.estadoRegistro) = 'ACTIVO'
              and lower(usuario.username) = lower(:username)
            order by registro.fechaAceptacion desc, registro.id desc
            """)
    List<ConsentRegistro> findActiveByUsername(@Param("username") String username);

    @Override
    @Query("""
            select registro
            from ConsentRegistro registro
            join fetch registro.usuario usuario
            join fetch registro.tipoConsentimiento tipo
            where upper(registro.estadoRegistro) = 'ACTIVO'
              and lower(usuario.username) = lower(:username)
              and tipo.id = :consentTypeId
              and registro.versionConsentimiento = :versionConsentimiento
            """)
    Optional<ConsentRegistro> findActiveByUsernameAndTypeVersion(
            @Param("username") String username,
            @Param("consentTypeId") Long consentTypeId,
            @Param("versionConsentimiento") Integer versionConsentimiento
    );
}
