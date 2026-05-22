package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping(path = "/metas/{goalId}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@gdrAccessPolicyService.canManageEvidenceCreation(authentication, #goalId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> createEvidence(
            @PathVariable Long goalId,
            @Valid @ModelAttribute GuardarEvidenciaRequest request,
            @RequestParam(name = "archivo", required = false) MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.createEvidence(goalId, request, archivo),
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

    @PutMapping(path = "/evidencias/{evidenceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@gdrAccessPolicyService.canManageEvidenceUpdate(authentication, #evidenceId)")
    public ResponseEntity<ApiResponse<DetalleEvidenciaResponse>> updateEvidence(
            @PathVariable Long evidenceId,
            @Valid @ModelAttribute GuardarEvidenciaRequest request,
            @RequestParam(name = "archivo", required = false) MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                evidenceService.updateEvidence(evidenceId, request, archivo),
                "Evidencia actualizada correctamente."
        ));
    }

    @GetMapping("/evidencias/{evidenceId}/archivo")
    @PreAuthorize("@gdrAccessPolicyService.canAccessEvidenceById(authentication, #evidenceId)")
    public ResponseEntity<?> downloadEvidenceFile(
            @PathVariable Long evidenceId,
            @RequestParam(name = "descarga", defaultValue = "true") boolean download
    ) {
        return evidenceService.downloadEvidenceFile(evidenceId, download);
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
