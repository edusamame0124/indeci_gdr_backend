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
import pe.gob.gdr.access.application.dto.request.GuardarEvaluacionFinalRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarRetroFinalRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.DetalleEvaluacionFinalResponse;
import pe.gob.gdr.access.application.dto.response.NotificarCalificacionMailResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvaluacionFinalResponse;
import pe.gob.gdr.access.application.service.GdrFinalEvaluationQualificationNotifyService;
import pe.gob.gdr.access.application.service.GdrFinalEvaluationService;

@RestController
@RequestMapping("/evaluacion-final")
public class GdrFinalEvaluationController {

    private final GdrFinalEvaluationService finalEvaluationService;
    private final GdrFinalEvaluationQualificationNotifyService qualificationNotifyService;

    public GdrFinalEvaluationController(
            GdrFinalEvaluationService finalEvaluationService,
            GdrFinalEvaluationQualificationNotifyService qualificationNotifyService
    ) {
        this.finalEvaluationService = finalEvaluationService;
        this.qualificationNotifyService = qualificationNotifyService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewFinalEvaluations(authentication)")
    public ResponseEntity<ApiResponse<List<ResumenEvaluacionFinalResponse>>> listFinalEvaluations(
            @RequestParam Long cycleId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.listFinalEvaluations(principal.getName(), cycleId),
                "Evaluaciones finales consultadas correctamente."
        ));
    }

    @GetMapping("/{evaluatedId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewFinalEvaluationByEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> getFinalEvaluation(
            @PathVariable Long evaluatedId,
            @RequestParam Long cycleId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.getFinalEvaluation(principal.getName(), evaluatedId, cycleId),
                "Detalle de evaluacion consultado correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageFinalEvaluationForAssignment(authentication, #request.assignmentId())")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> createFinalEvaluation(
            @RequestParam Long cycleId,
            @Valid @RequestBody GuardarEvaluacionFinalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.createFinalEvaluation(request, cycleId),
                "Evaluacion final registrada correctamente."
        ));
    }

    @PutMapping("/{evaluationId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageFinalEvaluationById(authentication, #evaluationId)")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> updateFinalEvaluation(
            @PathVariable Long evaluationId,
            @RequestParam Long cycleId,
            @Valid @RequestBody GuardarEvaluacionFinalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.updateFinalEvaluation(evaluationId, request, cycleId),
                "Evaluacion final actualizada correctamente."
        ));
    }

    @PutMapping("/{evaluationId}/retroalimentacion-final")
    @PreAuthorize("@gdrAccessPolicyService.canManageFinalEvaluationById(authentication, #evaluationId)")
    public ResponseEntity<ApiResponse<DetalleEvaluacionFinalResponse>> registrarRetroalimentacionFinal(
            @PathVariable Long evaluationId,
            @RequestParam Long cycleId,
            @Valid @RequestBody RegistrarRetroFinalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                finalEvaluationService.registrarRetroalimentacionFinal(evaluationId, request, cycleId),
                "Reunion de retroalimentacion final registrada correctamente."
        ));
    }

    @PostMapping("/{evaluationId}/notificar")
    @PreAuthorize("@gdrAccessPolicyService.canNotifyFinalEvaluationQualificationByEvaluator(authentication, #evaluationId)")
    public ResponseEntity<ApiResponse<NotificarCalificacionMailResponse>> notifyQualificationByEmail(
            @PathVariable Long evaluationId,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                qualificationNotifyService.notifyByEvaluator(principal.getName(), evaluationId),
                "Notificacion por correo enviada correctamente."
        ));
    }
}
