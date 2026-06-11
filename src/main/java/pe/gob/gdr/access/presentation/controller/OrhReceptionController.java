package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.ReviewOrhReceptionRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.OrhGoalChangeRequestItemResponse;
import pe.gob.gdr.access.application.dto.response.OrhGoalSubmissionItemResponse;
import pe.gob.gdr.access.application.service.OrhReceptionService;

@RestController
@RequestMapping("/orh/reception")
public class OrhReceptionController {

    private final OrhReceptionService orhReceptionService;

    public OrhReceptionController(OrhReceptionService orhReceptionService) {
        this.orhReceptionService = orhReceptionService;
    }

    @GetMapping("/change-requests")
    @PreAuthorize("@gdrAccessPolicyService.canViewOrhReception(authentication)")
    public ResponseEntity<ApiResponse<List<OrhGoalChangeRequestItemResponse>>> listChangeRequests(
            @RequestParam("cycleId") Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orhReceptionService.listChangeRequests(cycleId),
                "Solicitudes de modificacion consultadas correctamente."
        ));
    }

    @GetMapping("/submissions")
    @PreAuthorize("@gdrAccessPolicyService.canViewOrhReception(authentication)")
    public ResponseEntity<ApiResponse<List<OrhGoalSubmissionItemResponse>>> listSubmissions(
            @RequestParam("cycleId") Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orhReceptionService.listSubmissions(cycleId),
                "Envios a ORH consultados correctamente."
        ));
    }

    @PatchMapping("/change-requests/{id}/review")
    @PreAuthorize("@gdrAccessPolicyService.canViewOrhReception(authentication)")
    public ResponseEntity<ApiResponse<OrhGoalChangeRequestItemResponse>> reviewChangeRequest(
            @PathVariable Long id,
            @RequestParam("cycleId") Long cycleId,
            @Valid @RequestBody ReviewOrhReceptionRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orhReceptionService.reviewChangeRequest(id, request, principal.getName(), cycleId),
                "Solicitud de modificacion marcada como revisada por ORH."
        ));
    }

    @PatchMapping("/submissions/{id}/review")
    @PreAuthorize("@gdrAccessPolicyService.canViewOrhReception(authentication)")
    public ResponseEntity<ApiResponse<OrhGoalSubmissionItemResponse>> reviewSubmission(
            @PathVariable Long id,
            @RequestParam("cycleId") Long cycleId,
            @Valid @RequestBody ReviewOrhReceptionRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orhReceptionService.reviewSubmission(id, request, principal.getName(), cycleId),
                "Envio a ORH marcado como revisado por ORH."
        ));
    }
}
