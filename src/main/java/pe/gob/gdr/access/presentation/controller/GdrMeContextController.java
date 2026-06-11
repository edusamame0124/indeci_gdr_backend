package pe.gob.gdr.access.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.MeContextResponse;
import pe.gob.gdr.access.application.service.GdrAccessPolicyService;

/**
 * Contexto funcional del usuario autenticado por ciclo GDR.
 * Normativa: RPE 068-2020-SERVIR-PE (actor funcional por ciclo).
 */
@RestController
@RequestMapping("/gdr/ciclo")
public class GdrMeContextController {

    private final GdrAccessPolicyService policyService;

    public GdrMeContextController(GdrAccessPolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/{cycleId}/me/context")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCronograma()")
    public ResponseEntity<ApiResponse<MeContextResponse>> getMeContext(
            @PathVariable Long cycleId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                policyService.buildMeContext(authentication, cycleId),
                "Contexto del ciclo consultado correctamente."));
    }
}
