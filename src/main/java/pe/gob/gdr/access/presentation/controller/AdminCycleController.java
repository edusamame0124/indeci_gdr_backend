package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CycleOptionResponse;
import pe.gob.gdr.access.application.service.AdminAssignmentService;

@RestController
@RequestMapping("/admin/cycles")
public class AdminCycleController {

    private final AdminAssignmentService adminAssignmentService;

    public AdminCycleController(AdminAssignmentService adminAssignmentService) {
        this.adminAssignmentService = adminAssignmentService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<CycleOptionResponse>>> listCycles() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.listCycles(),
                "Ciclos consultados correctamente."
        ));
    }
}
