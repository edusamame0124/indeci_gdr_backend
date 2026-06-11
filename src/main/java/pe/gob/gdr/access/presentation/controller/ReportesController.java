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
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.dto.response.ReporteAvanceResponse;
import pe.gob.gdr.access.application.dto.response.ReporteDistribucionCalificacionResponse;
import pe.gob.gdr.access.application.dto.response.ReporteOportunidadMejoraResponse;
import pe.gob.gdr.access.application.dto.response.ReporteResultadoResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
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
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getProgressReport(evaluatedId, cycleId),
                "Reporte de avance consultado correctamente."
        ));
    }

    @GetMapping("/avance/exportar")
    public ResponseEntity<ByteArrayResource> exportProgressReport(
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            Principal principal
    ) {
        List<ReporteAvanceResponse> rows = reportesService.getProgressReport(evaluatedId, cycleId);
        return buildCsvResponse(
                reportesService.exportProgressCsv(rows, principal.getName()),
                "reporte_avance_gdr.csv"
        );
    }

    @GetMapping("/resultados")
    public ResponseEntity<ApiResponse<List<ReporteResultadoResponse>>> getResultsReport(
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getResultsReport(evaluatedId, cycleId),
                "Reporte de resultados consultado correctamente."
        ));
    }

    @GetMapping("/resultados/exportar")
    public ResponseEntity<ByteArrayResource> exportResultsReport(
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            Principal principal
    ) {
        List<ReporteResultadoResponse> rows = reportesService.getResultsReport(evaluatedId, cycleId);
        return buildCsvResponse(
                reportesService.exportResultsCsv(rows, principal.getName()),
                "reporte_resultados_gdr.csv"
        );
    }

    @GetMapping("/oportunidades-mejora")
    public ResponseEntity<ApiResponse<List<ReporteOportunidadMejoraResponse>>> getImprovementReport(
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getImprovementReport(evaluatedId, estadoCodigo, cycleId),
                "Reporte de oportunidades de mejora consultado correctamente."
        ));
    }

    @GetMapping("/confirmaciones")
    public ResponseEntity<ApiResponse<List<SolicitudConfirmacionResponse>>> getConfirmacionesReport() {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getConfirmacionesReport(),
                "Reporte de confirmaciones consultado correctamente."
        ));
    }

    @GetMapping("/confirmaciones/exportar")
    public ResponseEntity<ByteArrayResource> exportConfirmacionesReport(Principal principal) {
        List<SolicitudConfirmacionResponse> rows = reportesService.getConfirmacionesReport();
        return buildCsvResponse(
                reportesService.exportConfirmacionesCsv(rows, principal.getName()),
                "reporte_confirmaciones_gdr.csv"
        );
    }

    @GetMapping("/cie")
    public ResponseEntity<ApiResponse<List<CasoCieResponse>>> getCieReport() {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getCieReport(),
                "Reporte de casos CIE consultado correctamente."
        ));
    }

    @GetMapping("/cie/exportar")
    public ResponseEntity<ByteArrayResource> exportCieReport(Principal principal) {
        List<CasoCieResponse> rows = reportesService.getCieReport();
        return buildCsvResponse(
                reportesService.exportCieCsv(rows, principal.getName()),
                "reporte_cie_gdr.csv"
        );
    }

    @GetMapping("/distribucion-calificaciones")
    public ResponseEntity<ApiResponse<List<ReporteDistribucionCalificacionResponse>>> getDistribucionReport(
            @RequestParam Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportesService.getDistribucionCalificacionesReport(cycleId),
                "Reporte de distribución de calificaciones consultado correctamente."
        ));
    }

    @GetMapping("/distribucion-calificaciones/exportar")
    public ResponseEntity<ByteArrayResource> exportDistribucionReport(
            @RequestParam Long cycleId,
            Principal principal
    ) {
        List<ReporteDistribucionCalificacionResponse> rows = reportesService.getDistribucionCalificacionesReport(cycleId);
        return buildCsvResponse(
                reportesService.exportDistribucionCsv(rows, principal.getName()),
                "reporte_distribucion_calificaciones_gdr.csv"
        );
    }

    @GetMapping("/oportunidades-mejora/exportar")
    public ResponseEntity<ByteArrayResource> exportImprovementReport(
            @RequestParam Long cycleId,
            @RequestParam(name = "evaluatedId", required = false) Long evaluatedId,
            @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo,
            Principal principal
    ) {
        List<ReporteOportunidadMejoraResponse> rows = reportesService.getImprovementReport(evaluatedId, estadoCodigo, cycleId);
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
