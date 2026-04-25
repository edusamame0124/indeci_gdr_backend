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
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.GuardarEvaluacionFinalRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.DetalleEvaluacionFinalResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvaluacionFinalResponse;
import pe.gob.gdr.access.application.service.GdrFinalEvaluationService;

@RestController
@RequestMapping("/evaluacion-final")
public class GdrFinalEvaluationController {

    private final GdrFinalEvaluationService finalEvaluationService;

    public GdrFinalEvaluationController(GdrFinalEvaluationService finalEvaluationService) {
        this.finalEvaluationService = finalEvaluationService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewFinalEvaluations(authentication)")
    public ResponseEntity<ApiResponse<List<ResumenEvaluacionFinalResponse>>> listFinalEvaluations(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.listFinalEvaluations(principal.getName()),
                "Evaluaciones finales consultadas correctamente."
        ));
    }

    @GetMapping("/{evaluatedId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewFinalEvaluationByEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> getFinalEvaluation(
            @PathVariable Long evaluatedId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.getFinalEvaluation(principal.getName(), evaluatedId),
                "Detalle de evaluacion consultado correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageFinalEvaluationForAssignment(authentication, #request.assignmentId())")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> createFinalEvaluation(
            @Valid @RequestBody GuardarEvaluacionFinalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.createFinalEvaluation(request),
                "Evaluacion final registrada correctamente."
        ));
    }

    @PutMapping("/{evaluationId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageFinalEvaluationById(authentication, #evaluationId)")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> updateFinalEvaluation(
            @PathVariable Long evaluationId,
            @Valid @RequestBody GuardarEvaluacionFinalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.updateFinalEvaluation(evaluationId, request),
                "Evaluacion final actualizada correctamente."
        ));
    }
}
