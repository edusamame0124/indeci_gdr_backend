package pe.gob.gdr.access.presentation.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.NotificacionDetalleResponse;
import pe.gob.gdr.access.application.dto.response.NotificacionResumenResponse;
import pe.gob.gdr.access.application.service.NotificacionesService;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionesController {

    private final NotificacionesService notificacionesService;

    public NotificacionesController(NotificacionesService notificacionesService) {
        this.notificacionesService = notificacionesService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewNotifications(authentication)")
    public ResponseEntity<ApiResponse<List<NotificacionResumenResponse>>> listInbox(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionesService.listInbox(principal.getName()),
                "Bandeja de notificaciones consultada correctamente."
        ));
    }

    @GetMapping("/historial")
    @PreAuthorize("@gdrAccessPolicyService.canViewNotifications(authentication)")
    public ResponseEntity<ApiResponse<List<NotificacionResumenResponse>>> listHistory(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionesService.listHistory(principal.getName()),
                "Historial personal de notificaciones consultado correctamente."
        ));
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewNotifications(authentication)")
    public ResponseEntity<ApiResponse<NotificacionDetalleResponse>> getNotification(
            @PathVariable Long notificationId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionesService.getNotification(notificationId, principal.getName()),
                "Detalle de notificacion consultado correctamente."
        ));
    }

    @PostMapping("/{notificationId}/marcar-leida")
    @PreAuthorize("@gdrAccessPolicyService.canViewNotifications(authentication)")
    public ResponseEntity<ApiResponse<NotificacionDetalleResponse>> markAsRead(
            @PathVariable Long notificationId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionesService.markAsRead(notificationId, principal.getName()),
                "Notificacion marcada como leida correctamente."
        ));
    }
}
