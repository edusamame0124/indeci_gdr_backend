package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.GuardarEvidenciaRequest;
import pe.gob.gdr.access.application.dto.request.RevisionEvidenciaRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.DetalleEvidenciaResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvidenciaResponse;
import pe.gob.gdr.access.application.service.GdrEvidenceService;

@RestController
public class GdrEvidenceController {

    private final GdrEvidenceService evidenceService;

    public GdrEvidenceController(GdrEvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping("/metas/{goalId}/evidencias")
    @PreAuthorize("@gdrAccessPolicyService.canAccessGoalEvidence(authentication, #goalId)")
    public ResponseEntity<ApiResponse<List<ResumenEvidenciaResponse>>> listGoalEvidences(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.listGoalEvidences(goalId),
                "Evidencias consultadas correctamente."
        ));
    }

    @PostMapping("/metas/{goalId}/evidencias")
    @PreAuthorize("@gdrAccessPolicyService.canManageEvidenceCreation(authentication, #goalId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> createEvidence(
            @PathVariable Long goalId,
            @Valid @RequestBody GuardarEvidenciaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.createEvidence(goalId, request),
                "Evidencia registrada correctamente."
        ));
    }

    @GetMapping("/evidencias/{evidenceId}")
    @PreAuthorize("@gdrAccessPolicyService.canAccessEvidenceById(authentication, #evidenceId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> getEvidence(@PathVariable Long evidenceId) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.getEvidence(evidenceId),
                "Evidencia consultada correctamente."
        ));
    }

    @PutMapping("/evidencias/{evidenceId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageEvidenceUpdate(authentication, #evidenceId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> updateEvidence(
            @PathVariable Long evidenceId,
            @Valid @RequestBody GuardarEvidenciaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.updateEvidence(evidenceId, request),
                "Evidencia actualizada correctamente."
        ));
    }

    @PostMapping("/evidencias/{evidenceId}/revision")
    @PreAuthorize("@gdrAccessPolicyService.canReviewEvidence(authentication, #evidenceId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> reviewEvidence(
            @PathVariable Long evidenceId,
            @Valid @RequestBody RevisionEvidenciaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.reviewEvidence(evidenceId, request),
                "Revision de evidencia registrada correctamente."
        ));
    }
}
