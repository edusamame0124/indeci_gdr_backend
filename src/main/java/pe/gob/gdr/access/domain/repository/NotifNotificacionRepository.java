package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.NotifNotificacion;

public interface NotifNotificacionRepository {

    List<NotifNotificacion> findInboxByUsername(String username);

    List<NotifNotificacion> findHistoryByUsername(String username);

    Optional<NotifNotificacion> findByIdAndUsername(Long notificationId, String username);

    NotifNotificacion save(NotifNotificacion notificacion);
}
