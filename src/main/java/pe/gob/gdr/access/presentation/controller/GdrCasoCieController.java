package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.ResolverCasoCieRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.service.GdrCasoCieService;

/**
 * Endpoints de la bandeja del Comité Institucional de Evaluación (CIE).
 * Normativa: RPE 068-2020-SERVIR-PE Art. 42 (decisión definitiva).
 */
@RestController
@RequestMapping("/gdr/cie/casos")
public class GdrCasoCieController {

    private final GdrCasoCieService casoCieService;

    public GdrCasoCieController(GdrCasoCieService casoCieService) {
        this.casoCieService = casoCieService;
    }

    /** Bandeja de casos con semáforo del plazo de convocatoria (3 días hábiles). */
    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCie()")
    public ResponseEntity<ApiResponse<List<CasoCieResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(
                casoCieService.listarBandeja(),
                "Bandeja de casos CIE consultada."));
    }

    @GetMapping("/{casoId}")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCie()")
    public ResponseEntity<ApiResponse<CasoCieResponse>> getDetalle(@PathVariable Long casoId) {
        return ResponseEntity.ok(ApiResponse.ok(
                casoCieService.getDetalle(casoId),
                "Detalle del caso CIE consultado."));
    }

    /** Registra la decisión definitiva del CIE: CONFIRMA o MODIFICA. */
    @PutMapping("/{casoId}/resolver")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canResolverCasosCie()")
    public ResponseEntity<ApiResponse<CasoCieResponse>> resolver(
            @PathVariable Long casoId,
            @Valid @RequestBody ResolverCasoCieRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        String username = principal != null ? principal.getUsername() : "sistema-gdr";
        return ResponseEntity.ok(ApiResponse.ok(
                casoCieService.resolver(casoId, request, username),
                "Decisión del CIE registrada. La resolución es definitiva."));
    }

    /** P6-04 — Acta de sesión del CIE en PDF. */
    @GetMapping("/{casoId}/acta-pdf")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCie()")
    public ResponseEntity<Resource> downloadActaPdf(@PathVariable Long casoId) {
        return casoCieService.downloadActaCiePdf(casoId);
    }
}
