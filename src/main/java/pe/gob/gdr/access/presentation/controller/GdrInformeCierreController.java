package pe.gob.gdr.access.presentation.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.GenerarInformeCierreRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarRemisionRequest;
import pe.gob.gdr.access.application.dto.response.AlertaEvaluacionesSinNotificarResponse;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.InformeCierreAlertaResponse;
import pe.gob.gdr.access.application.dto.response.InformeCierreConsolidadoResponse;
import pe.gob.gdr.access.application.dto.response.RemisionResponse;
import pe.gob.gdr.access.application.service.GdrInformeCierreService;
import pe.gob.gdr.access.application.service.GdrInformeRemisionService;

@RestController
@RequestMapping("/gdr/informe-cierre")
public class GdrInformeCierreController {

    private final GdrInformeCierreService informeCierreService;
    private final GdrInformeRemisionService remisionService;

    public GdrInformeCierreController(
            GdrInformeCierreService informeCierreService,
            GdrInformeRemisionService remisionService) {
        this.informeCierreService = informeCierreService;
        this.remisionService = remisionService;
    }

    @GetMapping("/vista-previa")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<InformeCierreConsolidadoResponse>> vistaPrevia() {
        return ResponseEntity.ok(ApiResponse.ok(
                informeCierreService.obtenerVistaPrevia(),
                "Vista previa del informe de cierre consultada."));
    }

    @GetMapping("/alerta-val06")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<InformeCierreAlertaResponse>> alertaVal06() {
        return ResponseEntity.ok(ApiResponse.ok(
                informeCierreService.obtenerAlertaVal06(),
                "Alerta VAL-06 del informe de cierre consultada."));
    }

    @GetMapping("/alerta-val13a")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<AlertaEvaluacionesSinNotificarResponse>> alertaVal13a() {
        return ResponseEntity.ok(ApiResponse.ok(
                informeCierreService.obtenerAlertaVal13a(),
                "Alerta VAL-13A: evaluaciones sin retroalimentación final consultada."));
    }

    @GetMapping("/historial")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<List<InformeCierreConsolidadoResponse>>> historial() {
        return ResponseEntity.ok(ApiResponse.ok(
                informeCierreService.listarHistorial(),
                "Historial de informes de cierre consultado."));
    }

    @PostMapping("/generar")
    @PreAuthorize("@gdrAccessPolicyService.canGenerarInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<InformeCierreConsolidadoResponse>> generar(
            @Valid @RequestBody GenerarInformeCierreRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "sistema-gdr";
        return ResponseEntity.ok(ApiResponse.ok(
                informeCierreService.generar(request, username),
                "Informe de cierre generado y registrado en borrador."));
    }

    @GetMapping("/{informeId}/pdf")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long informeId) {
        return informeCierreService.downloadPdf(informeId);
    }

    @GetMapping("/{informeId}/exportar-csv")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<Resource> exportCsv(@PathVariable Long informeId, Principal principal) {
        String username = principal != null ? principal.getName() : "sistema-gdr";
        return informeCierreService.exportCsv(informeId, username);
    }

    // ── Remisión a SERVIR — POSIBLE_CAMBIO_RRHH_GDR_008 ─────────────────────

    /**
     * Lista el historial de remisiones registradas para un informe de cierre.
     */
    @GetMapping("/{informeId}/remision")
    @PreAuthorize("@gdrAccessPolicyService.canViewInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<List<RemisionResponse>>> listarRemisiones(
            @PathVariable Long informeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                remisionService.listar(informeId),
                "Historial de remisiones del informe consultado."));
    }

    /**
     * Registra la evidencia de remisión del informe a SERVIR.
     * P0: manual controlado. Marca el informe como REMITIDO.
     * P1 (comentado): flujo de aprobación interno previo.
     * P2 (comentado): integración con API SERVIR.
     */
    @PostMapping("/{informeId}/remision")
    @PreAuthorize("@gdrAccessPolicyService.canGenerarInformeCierre(authentication)")
    public ResponseEntity<ApiResponse<RemisionResponse>> registrarRemision(
            @PathVariable Long informeId,
            @Valid @RequestBody RegistrarRemisionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "sistema-gdr";
        return ResponseEntity.ok(ApiResponse.ok(
                remisionService.registrar(informeId, request, username),
                "Remisión a SERVIR registrada correctamente. El informe queda marcado como REMITIDO."));
    }
}
