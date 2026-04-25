package pe.gob.gdr.access.application.service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.response.NotificacionDetalleResponse;
import pe.gob.gdr.access.application.dto.response.NotificacionResumenResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.NotifNotificacion;
import pe.gob.gdr.access.domain.model.NotifPlantilla;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.NotifNotificacionRepository;
import pe.gob.gdr.access.domain.repository.NotifPlantillaRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class NotificacionesService {

    public static final String DOCUMENTO_FIRMADO_REGISTRADO = "DOCUMENTO_FIRMADO_REGISTRADO";
    public static final String OPORTUNIDAD_MEJORA_REGISTRADA = "OPORTUNIDAD_MEJORA_REGISTRADA";
    public static final String SEGUIMIENTO_MEJORA_REGISTRADO = "SEGUIMIENTO_MEJORA_REGISTRADO";
    public static final String OPORTUNIDAD_MEJORA_CERRADA = "OPORTUNIDAD_MEJORA_CERRADA";

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionesService.class);

    private final NotifNotificacionRepository notificacionRepository;
    private final NotifPlantillaRepository plantillaRepository;
    private final UserRepository userRepository;
    private final AuditTrailService auditTrailService;

    public NotificacionesService(
            NotifNotificacionRepository notificacionRepository,
            NotifPlantillaRepository plantillaRepository,
            UserRepository userRepository,
            AuditTrailService auditTrailService
    ) {
        this.notificacionRepository = notificacionRepository;
        this.plantillaRepository = plantillaRepository;
        this.userRepository = userRepository;
        this.auditTrailService = auditTrailService;
    }

    public List<NotificacionResumenResponse> listInbox(String username) {
        return notificacionRepository.findInboxByUsername(username).stream()
                .map(this::mapSummary)
                .toList();
    }

    public List<NotificacionResumenResponse> listHistory(String username) {
        return notificacionRepository.findHistoryByUsername(username).stream()
                .map(this::mapSummary)
                .toList();
    }

    @Transactional
    public NotificacionDetalleResponse getNotification(Long notificationId, String username) {
        NotifNotificacion notification = resolveOwnedNotification(notificationId, username);
        return mapDetail(notification);
    }

    @Transactional
    public NotificacionDetalleResponse markAsRead(Long notificationId, String username) {
        NotifNotificacion notification = resolveOwnedNotification(notificationId, username);
        if (!"LEIDA".equalsIgnoreCase(notification.getEstadoNotificacion())) {
            notification.setEstadoNotificacion("LEIDA");
            notification.setFechaLectura(LocalDateTime.now());
            notification = notificacionRepository.save(notification);
        }
        auditTrailService.recordEvent(
                "NOTIFICACION_LEIDA",
                username,
                "Notificacion marcada como leida: " + notificationId,
                null
        );
        return mapDetail(notification);
    }

    @Transactional
    public void emitForUser(String username, String templateCode, String businessReference) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario destino de la notificacion."));
            NotifPlantilla template = plantillaRepository.findActiveByCodigo(templateCode)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontro la plantilla de notificacion solicitada."));

            String resolvedReference = businessReference == null || businessReference.isBlank()
                    ? "SIN_REFERENCIA"
                    : businessReference.trim();

            notificacionRepository.save(NotifNotificacion.builder()
                    .usuarioDestino(user)
                    .plantilla(template)
                    .codigoEvento(templateCode)
                    .tituloNotificacion(template.getAsunto())
                    .mensajeNotificacion(resolveMessage(template.getCuerpoMensaje(), resolvedReference))
                    .estadoNotificacion("NO_LEIDA")
                    .fechaEnvio(LocalDateTime.now())
                    .referenciaNegocio(resolvedReference)
                    .estadoRegistro("ACTIVO")
                    .build());
        } catch (Exception exception) {
            LOGGER.warn(
                    "No se pudo registrar la notificacion del evento {} para el usuario {}. El flujo principal continuara.",
                    templateCode,
                    username,
                    exception
            );
        }
    }

    private NotifNotificacion resolveOwnedNotification(Long notificationId, String username) {
        return notificacionRepository.findByIdAndUsername(notificationId, username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la notificacion solicitada para el usuario autenticado."));
    }

    private String resolveMessage(String messageTemplate, String businessReference) {
        if (messageTemplate == null || messageTemplate.isBlank()) {
            return "Notificacion del ciclo GDR. Referencia: " + businessReference + ".";
        }
        return messageTemplate.replace("{referencia}", businessReference);
    }

    private NotificacionResumenResponse mapSummary(NotifNotificacion notification) {
        return new NotificacionResumenResponse(
                notification.getId(),
                notification.getCodigoEvento(),
                notification.getTituloNotificacion(),
                notification.getMensajeNotificacion(),
                notification.getEstadoNotificacion(),
                notification.getFechaEnvio(),
                notification.getFechaLectura(),
                notification.getReferenciaNegocio()
        );
    }

    private NotificacionDetalleResponse mapDetail(NotifNotificacion notification) {
        return new NotificacionDetalleResponse(
                notification.getId(),
                notification.getCodigoEvento(),
                notification.getTituloNotificacion(),
                notification.getMensajeNotificacion(),
                notification.getEstadoNotificacion(),
                notification.getFechaEnvio(),
                notification.getFechaLectura(),
                notification.getReferenciaNegocio(),
                notification.getPlantilla().getCodigoPlantilla(),
                notification.getPlantilla().getNombrePlantilla(),
                notification.getPlantilla().getCanal().getNombreCanal()
        );
    }
}
