package pe.gob.gdr.access.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CronogramaEtapaRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CicloConCronogramaResponse;
import pe.gob.gdr.access.application.dto.response.CronogramaEtapaResponse;
import pe.gob.gdr.access.application.service.GdrCronogramaService;

/**
 * Endpoints del cronograma del ciclo GDR.
 * Normativa base: RPE 068-2020-SERVIR-PE Art. 14.
 */
@RestController
@RequestMapping("/gdr/ciclo")
public class GdrCronogramaController {

    private final GdrCronogramaService cronogramaService;

    public GdrCronogramaController(GdrCronogramaService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    /** Cronograma del ciclo activo — todos los actores con acceso GDR. */
    @GetMapping("/activo/cronograma")
    @PreAuthorize("@gdrAccessPolicyService.resolveFeatureAccessByAuth(authentication).canViewCronograma()")
    public ResponseEntity<ApiResponse<CicloConCronogramaResponse>> getCronogramaActivo() {
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.getCicloCronogramaActivo(),
                "Cronograma consultado correctamente."));
    }

    /** Cronograma de un ciclo específico — ORH y ADMIN. */
    @GetMapping("/{cycleId}/cronograma")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<CicloConCronogramaResponse>> getCronograma(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.getCicloCronograma(cycleId),
                "Cronograma consultado correctamente."));
    }

    /** Upsert de una etapa del cronograma — solo ORH. */
    @PutMapping("/{cycleId}/cronograma/{etapa}")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<CronogramaEtapaResponse>> upsertEtapa(
            @PathVariable Long cycleId,
            @PathVariable String etapa,
            @RequestBody CronogramaEtapaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.upsertEtapa(cycleId, etapa, request),
                "Etapa del cronograma actualizada."));
    }
}
