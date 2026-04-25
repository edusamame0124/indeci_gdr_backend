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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.IndicatorUpsertRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.IndicatorResponse;
import pe.gob.gdr.access.application.service.GdrIndicatorService;

@RestController
@RequestMapping("/indicators")
public class GdrIndicatorController {

    private final GdrIndicatorService gdrIndicatorService;

    public GdrIndicatorController(GdrIndicatorService gdrIndicatorService) {
        this.gdrIndicatorService = gdrIndicatorService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewIndicators(authentication)")
    public ResponseEntity<ApiResponse<List<IndicatorResponse>>> listIndicators() {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrIndicatorService.listIndicators(),
                "Indicadores consultados correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageIndicators(authentication)")
    public ResponseEntity<ApiResponse<IndicatorResponse>> createIndicator(
            @Valid @RequestBody IndicatorUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrIndicatorService.createIndicator(request),
                "Indicador registrado correctamente."
        ));
    }

    @PutMapping("/{indicatorId}")
    @PreAuthorize("@gdrAccessPolicyService.canManageIndicators(authentication)")
    public ResponseEntity<ApiResponse<IndicatorResponse>> updateIndicator(
            @PathVariable Long indicatorId,
            @Valid @RequestBody IndicatorUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrIndicatorService.updateIndicator(indicatorId, request),
                "Indicador actualizado correctamente."
        ));
    }
}
