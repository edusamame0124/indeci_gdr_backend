package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.SolicitudConfirmacionRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
import pe.gob.gdr.access.application.service.GdrSolicitudConfirmacionService;

/**
 * Endpoints de solicitud de confirmación de calificación.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 41 (5 días hábiles, bloqueo VAL-04).
 */
@RestController
@RequestMapping("/gdr/confirmacion")
public class GdrConfirmacionController {

    private final GdrSolicitudConfirmacionService solicitudService;

    public GdrConfirmacionController(GdrSolicitudConfirmacionService solicitudService) {
        this.solicitudService = solicitudService;
    }

    /** El evaluado presenta su solicitud de confirmación; se deriva al CIE automáticamente. */
    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canSolicitarConfirmacion()")
    public ResponseEntity<ApiResponse<SolicitudConfirmacionResponse>> solicitar(
            @Valid @RequestBody SolicitudConfirmacionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                solicitudService.solicitar(request, authentication.getName()),
                "Solicitud de confirmación registrada y derivada al CIE."));
    }

    /** Estado de la solicitud asociada a una evaluación final (null si no existe). */
    @GetMapping("/evaluacion/{finalEvaluationId}")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewConfirmacion()")
    public ResponseEntity<ApiResponse<SolicitudConfirmacionResponse>> getByEvaluacion(
            @PathVariable Long finalEvaluationId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                solicitudService.findByEvaluacion(finalEvaluationId, authentication.getName()).orElse(null),
                "Solicitud de confirmación consultada."));
    }

    /** Listado de solicitudes del ciclo activo — supervisión ORH/CIE. */
    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCie()")
    public ResponseEntity<ApiResponse<List<SolicitudConfirmacionResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(
                solicitudService.listarCicloActivo(),
                "Solicitudes de confirmación del ciclo activo."));
    }
}
