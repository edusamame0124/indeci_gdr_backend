package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.NotifNotificacion;
import pe.gob.gdr.access.domain.repository.NotifNotificacionRepository;

@Repository
public interface JpaNotifNotificacionRepository
        extends JpaRepository<NotifNotificacion, Long>, NotifNotificacionRepository {

    @Override
    @Query("""
            select notificacion
            from NotifNotificacion notificacion
            join fetch notificacion.usuarioDestino usuario
            join fetch notificacion.plantilla plantilla
            join fetch plantilla.canal canal
            where upper(notificacion.estadoRegistro) = 'ACTIVO'
              and upper(usuario.status) = 'ACTIVE'
              and lower(usuario.username) = lower(:username)
            order by case when upper(notificacion.estadoNotificacion) = 'NO_LEIDA' then 0 else 1 end,
                     notificacion.fechaEnvio desc,
                     notificacion.id desc
            """)
    List<NotifNotificacion> findInboxByUsername(@Param("username") String username);

    @Override
    @Query("""
            select notificacion
            from NotifNotificacion notificacion
            join fetch notificacion.usuarioDestino usuario
            join fetch notificacion.plantilla plantilla
            join fetch plantilla.canal canal
            where upper(notificacion.estadoRegistro) = 'ACTIVO'
              and upper(usuario.status) = 'ACTIVE'
              and lower(usuario.username) = lower(:username)
            order by notificacion.fechaEnvio desc, notificacion.id desc
            """)
    List<NotifNotificacion> findHistoryByUsername(@Param("username") String username);

    @Override
    @Query("""
            select notificacion
            from NotifNotificacion notificacion
            join fetch notificacion.usuarioDestino usuario
            join fetch notificacion.plantilla plantilla
            join fetch plantilla.canal canal
            where notificacion.id = :notificationId
              and upper(notificacion.estadoRegistro) = 'ACTIVO'
              and upper(usuario.status) = 'ACTIVE'
              and lower(usuario.username) = lower(:username)
            """)
    Optional<NotifNotificacion> findByIdAndUsername(
            @Param("notificationId") Long notificationId,
            @Param("username") String username
    );
}
