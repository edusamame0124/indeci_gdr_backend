package pe.gob.gdr.access.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.ReporteAvanceResponse;
import pe.gob.gdr.access.application.dto.response.ReporteOportunidadMejoraResponse;
import pe.gob.gdr.access.application.dto.response.ReporteResultadoResponse;
import pe.gob.gdr.access.application.service.ReportesService;

@RestController
@RequestMapping("/reportes")
@PreAuthorize("@gdrAccessPolicyService.canViewReports(authentication)")
public class ReportesController {

    private final ReportesService reportesService;

    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    @GetMapping("/avance")
    public ResponseEntity<ApiResponse<List<ReporteAvanceResponse>>> getProgressReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getProgressReport(evaluatedId),
                "Reporte de avance consultado correctamente."
        ));
    }

    @GetMapping("/avance/exportar")
    public ResponseEntity<ByteArrayResource> exportProgressReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            Principal principal
    ) {
        List<ReporteAvanceResponse> rows = reportesService.getProgressReport(evaluatedId);
        return buildCsvResponse(
                reportesService.exportProgressCsv(rows, principal.getName()),
                "reporte_avance_gdr.csv"
        );
    }

    @GetMapping("/resultados")
    public ResponseEntity<ApiResponse<List<ReporteResultadoResponse>>> getResultsReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getResultsReport(evaluatedId),
                "Reporte de resultados consultado correctamente."
        ));
    }

    @GetMapping("/resultados/exportar")
    public ResponseEntity<ByteArrayResource> exportResultsReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            Principal principal
    ) {
        List<ReporteResultadoResponse> rows = reportesService.getResultsReport(evaluatedId);
        return buildCsvResponse(
                reportesService.exportResultsCsv(rows, principal.getName()),
                "reporte_resultados_gdr.csv"
        );
    }

    @GetMapping("/oportunidades-mejora")
    public ResponseEntity<ApiResponse<List<ReporteOportunidadMejoraResponse>>> getImprovementReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getImprovementReport(evaluatedId, estadoCodigo),
                "Reporte de oportunidades de mejora consultado correctamente."
        ));
    }

    @GetMapping("/oportunidades-mejora/exportar")
    public ResponseEntity<ByteArrayResource> exportImprovementReport(
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo,
            Principal principal
    ) {
        List<ReporteOportunidadMejoraResponse> rows = reportesService.getImprovementReport(evaluatedId, estadoCodigo);
        return buildCsvResponse(
                reportesService.exportImprovementCsv(rows, principal.getName()),
                "reporte_oportunidades_mejora_gdr.csv"
        );
    }

    private ResponseEntity<ByteArrayResource> buildCsvResponse(byte[] content, String fileName) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(content));
    }
}
