package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.RegistrarOportunidadMejoraRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarSeguimientoMejoraRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.OportunidadMejoraDetalleResponse;
import pe.gob.gdr.access.application.dto.response.OportunidadMejoraResumenResponse;
import pe.gob.gdr.access.application.service.GdrImprovementService;

@RestController
@RequestMapping("/oportunidades-mejora")
public class GdrImprovementController {

    private final GdrImprovementService improvementService;

    public GdrImprovementController(GdrImprovementService improvementService) {
        this.improvementService = improvementService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canAccessImprovementsForEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<ApiResponse<List<OportunidadMejoraResumenResponse>>> listImprovements(
            @RequestParam("evaluatedId") Long evaluatedId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                improvementService.listImprovements(evaluatedId),
                "Oportunidades de mejora consultadas correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canCreateImprovementForEvaluated(authentication, #request.evaluatedId())")
    public ResponseEntity<ApiResponse<OportunidadMejoraDetalleResponse>> createImprovement(
            @Valid @RequestBody RegistrarOportunidadMejoraRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                improvementService.createImprovement(request, principal.getName()),
                "Oportunidad de mejora registrada correctamente."
        ));
    }

    @GetMapping("/{opportunityId}")
    @PreAuthorize("@gdrAccessPolicyService.canAccessImprovementById(authentication, #opportunityId)")
    public ResponseEntity<ApiResponse<OportunidadMejoraDetalleResponse>> getImprovement(@PathVariable Long opportunityId) {
        return ResponseEntity.ok(ApiResponse.ok(
                improvementService.getImprovement(opportunityId),
                "Detalle de oportunidad de mejora consultado correctamente."
        ));
    }

    @PutMapping("/{opportunityId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageImprovementById(authentication, #opportunityId)")
    public ResponseEntity<ApiResponse<OportunidadMejoraDetalleResponse>> updateImprovement(
            @PathVariable Long opportunityId,
            @Valid @RequestBody RegistrarOportunidadMejoraRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                improvementService.updateImprovement(opportunityId, request, principal.getName()),
                "Oportunidad de mejora actualizada correctamente."
        ));
    }

    @PostMapping("/{opportunityId}/seguimiento")
    @PreAuthorize("@gdrAccessPolicyService.canFollowupImprovementById(authentication, #opportunityId)")
    public ResponseEntity<ApiResponse<OportunidadMejoraDetalleResponse>> registerFollowup(
            @PathVariable Long opportunityId,
            @Valid @RequestBody RegistrarSeguimientoMejoraRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                improvementService.registerFollowup(opportunityId, request, principal.getName()),
                "Seguimiento registrado correctamente."
        ));
    }
}
