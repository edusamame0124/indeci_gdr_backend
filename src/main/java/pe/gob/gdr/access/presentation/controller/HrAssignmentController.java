package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.HrAssignmentSummaryResponse;
import pe.gob.gdr.access.application.service.HrAssignmentService;

@RestController
@RequestMapping("/hr/assignments")
public class HrAssignmentController {

    private final HrAssignmentService hrAssignmentService;

    public HrAssignmentController(HrAssignmentService hrAssignmentService) {
        this.hrAssignmentService = hrAssignmentService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewAssignments(authentication)")
    public ResponseEntity<ApiResponse<List<HrAssignmentSummaryResponse>>> listAssignments(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                hrAssignmentService.listAssignments(authentication.getName()),
                "Asignaciones consultadas correctamente."
        ));
    }
}
