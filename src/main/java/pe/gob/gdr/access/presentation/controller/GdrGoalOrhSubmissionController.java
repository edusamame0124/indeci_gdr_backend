package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CreateGoalOrhSubmissionRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.GoalOrhSubmissionResponse;
import pe.gob.gdr.access.application.service.GdrGoalOrhSubmissionService;

@RestController
public class GdrGoalOrhSubmissionController {

    private final GdrGoalOrhSubmissionService goalOrhSubmissionService;

    public GdrGoalOrhSubmissionController(GdrGoalOrhSubmissionService goalOrhSubmissionService) {
        this.goalOrhSubmissionService = goalOrhSubmissionService;
    }

    @PostMapping("/goals/{goalId}/orh-submissions")
    @PreAuthorize("@gdrAccessPolicyService.canSubmitGoalToOrh(authentication, #goalId)")
    public ResponseEntity<ApiResponse<GoalOrhSubmissionResponse>> createGoalOrhSubmission(
            @PathVariable Long goalId,
            @Valid @RequestBody CreateGoalOrhSubmissionRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalOrhSubmissionService.createGoalOrhSubmission(goalId, request, principal.getName()),
                "Envio a ORH registrado correctamente."
        ));
    }
}
