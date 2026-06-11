package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.GdrSeguimientoRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.GdrSeguimientoResponse;
import pe.gob.gdr.access.application.dto.response.ResumenSeguimientoResponse;
import pe.gob.gdr.access.application.service.GdrSeguimientoService;

/**
 * Endpoints de seguimiento periódico del ciclo GDR.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 26-32 (seguimiento mínimo 6 meses).
 */
@RestController
@RequestMapping("/gdr/seguimiento")
public class GdrSeguimientoController {

    private final GdrSeguimientoService seguimientoService;

    public GdrSeguimientoController(GdrSeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    /** Resumen + listado de reuniones para una asignación. */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewSeguimiento()")
    public ResponseEntity<ApiResponse<ResumenSeguimientoResponse>> getResumen(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                seguimientoService.getResumen(assignmentId),
                "Resumen de seguimiento consultado."));
    }

    /** Listado de todas las reuniones de un ciclo — ORH. */
    @GetMapping("/ciclo/{cycleId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<List<GdrSeguimientoResponse>>> listarPorCiclo(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                seguimientoService.listarPorCiclo(cycleId),
                "Reuniones del ciclo consultadas."));
    }

    /** Registra una nueva reunión de seguimiento — evaluador. */
    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canRegistrarSeguimiento()")
    public ResponseEntity<ApiResponse<GdrSeguimientoResponse>> registrar(
            @Valid @RequestBody GdrSeguimientoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                seguimientoService.registrar(request, authentication.getName()),
                "Reunión registrada correctamente."));
    }

    /** El evaluado confirma la reunión registrada por el evaluador. */
    @PostMapping("/{id}/consentimiento")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canRegistrarSeguimiento()")
    public ResponseEntity<ApiResponse<GdrSeguimientoResponse>> consentimiento(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                seguimientoService.registrarConsentimiento(id, authentication.getName()),
                "Consentimiento registrado correctamente."));
    }

    /** Elimina una reunión — solo ORH o quien la registró. */
    @DeleteMapping("/{id}")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        seguimientoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Reunión eliminada."));
    }
}
