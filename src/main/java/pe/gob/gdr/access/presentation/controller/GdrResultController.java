package pe.gob.gdr.access.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.ResultadoResponse;
import pe.gob.gdr.access.application.service.GdrResultService;

@RestController
@RequestMapping("/resultados")
public class GdrResultController {

    private final GdrResultService resultService;

    public GdrResultController(GdrResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/{evaluatedId}")
    @PreAuthorize("@gdrAccessPolicyService.canViewResultByEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<ApiResponse<ResultadoResponse>> getResult(@PathVariable Long evaluatedId) {
        return ResponseEntity.ok(ApiResponse.ok(
                resultService.getResultByEvaluatedId(evaluatedId),
                "Resultado consolidado consultado correctamente."
        ));
    }
}
