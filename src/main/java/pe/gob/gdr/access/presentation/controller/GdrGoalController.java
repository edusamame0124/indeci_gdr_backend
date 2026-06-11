package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.GoalCalificacionRequest;
import pe.gob.gdr.access.application.dto.request.GoalUpsertRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.GoalDetailResponse;
import pe.gob.gdr.access.application.dto.response.GoalSummaryResponse;
import pe.gob.gdr.access.application.service.GdrGoalService;

@RestController
@RequestMapping("/goals")
public class GdrGoalController {

    private final GdrGoalService gdrGoalService;

    public GdrGoalController(GdrGoalService gdrGoalService) {
        this.gdrGoalService = gdrGoalService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewGoals(authentication)")
    public ResponseEntity<ApiResponse<List<GoalSummaryResponse>>> listGoals(
            @RequestParam Long cycleId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrGoalService.listGoals(authentication.getName(), cycleId),
                "Metas consultadas correctamente."
        ));
    }

    @GetMapping("/{goalId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewGoals(authentication)")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> getGoal(
            @PathVariable Long goalId,
            @RequestParam Long cycleId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrGoalService.getGoal(authentication.getName(), goalId, cycleId),
                "Meta consultada correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageGoals(authentication)")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> createGoal(
            @RequestParam Long cycleId,
            @Valid @RequestBody GoalUpsertRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrGoalService.createGoal(authentication.getName(), cycleId, request),
                "Meta registrada correctamente."
        ));
    }

    @PutMapping("/{goalId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageGoals(authentication)")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> updateGoal(
            @PathVariable Long goalId,
            @RequestParam Long cycleId,
            @Valid @RequestBody GoalUpsertRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrGoalService.updateGoal(authentication.getName(), goalId, cycleId, request),
                "Meta actualizada correctamente."
        ));
    }

    @PutMapping("/{goalId}/calificacion")
    @PreAuthorize("@gdrAccessPolicyService.canRateGoalAchievement(authentication, #goalId)")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> rateGoalAchievement(
            @PathVariable Long goalId,
            @RequestParam Long cycleId,
            @Valid @RequestBody GoalCalificacionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrGoalService.rateGoalAchievement(authentication.getName(), goalId, cycleId, request),
                "Calificacion de meta registrada correctamente."
        ));
    }
}
