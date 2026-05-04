package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CreateGoalChangeRequestRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.GoalChangeRequestResponse;
import pe.gob.gdr.access.application.service.GdrGoalChangeRequestService;

@RestController
public class GdrGoalChangeRequestController {

    private final GdrGoalChangeRequestService goalChangeRequestService;

    public GdrGoalChangeRequestController(GdrGoalChangeRequestService goalChangeRequestService) {
        this.goalChangeRequestService = goalChangeRequestService;
    }

    @PostMapping("/goals/{goalId}/change-requests")
    @PreAuthorize("@gdrAccessPolicyService.canCreateGoalChangeRequest(authentication, #goalId)")
    public ResponseEntity<ApiResponse<GoalChangeRequestResponse>> createGoalChangeRequest(
            @PathVariable Long goalId,
            @Valid @RequestBody CreateGoalChangeRequestRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalChangeRequestService.createGoalChangeRequest(goalId, request, principal.getName()),
                "Solicitud de modificacion registrada correctamente."
        ));
    }
}
