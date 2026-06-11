package pe.gob.gdr.access.application.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.GenerarInformeCierreRequest;
import pe.gob.gdr.access.application.dto.response.AlertaEvaluacionesSinNotificarResponse;
import pe.gob.gdr.access.application.dto.response.InformeCierreAlertaResponse;
import pe.gob.gdr.access.application.dto.response.InformeCierreConsolidadoResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrInformeCierre;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRepository;

@Service
public class GdrInformeCierreService {

    private static final String REF_VAL06 = "RPE 068-2020-SERVIR-PE Art. 55";

    private final ActiveCycleRepository activeCycleRepository;
    private final GdrInformeCierreRepository informeRepository;
    private final GdrInformeCierreConsolidador consolidador;
    private final GdrValidacionNormativaService validacionNormativaService;
    private final FormatoInformeCierrePdfExporter pdfExporter;
    private final AuditTrailService auditTrailService;

    public GdrInformeCierreService(
            ActiveCycleRepository activeCycleRepository,
            GdrInformeCierreRepository informeRepository,
            GdrInformeCierreConsolidador consolidador,
            GdrValidacionNormativaService validacionNormativaService,
            FormatoInformeCierrePdfExporter pdfExporter,
            AuditTrailService auditTrailService
    ) {
        this.activeCycleRepository = activeCycleRepository;
        this.informeRepository = informeRepository;
        this.consolidador = consolidador;
        this.validacionNormativaService = validacionNormativaService;
        this.pdfExporter = pdfExporter;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public InformeCierreConsolidadoResponse obtenerVistaPrevia() {
        ActiveCycle cycle = requireActiveCycle();
        GdrInformeCierreConsolidador.InformeCierreSnapshot snap = consolidador.consolidar(cycle);
        return mapSnapshot(cycle, null, GdrInformeCierre.ESTADO_BORRADOR, snap, null, null, null);
    }

    @Transactional(readOnly = true)
    public InformeCierreAlertaResponse obtenerAlertaVal06() {
        ActiveCycle cycle = requireActiveCycle();
        return buildAlertaVal06(cycle);
    }

    @Transactional(readOnly = true)
    public AlertaEvaluacionesSinNotificarResponse obtenerAlertaVal13a() {
        ActiveCycle cycle = requireActiveCycle();
        List<String> sinNotificar = validacionNormativaService.findNombresSinNotificarEnCiclo(cycle.getId());
        int total = sinNotificar.size();
        String mensaje = total == 0
                ? "Todas las evaluaciones del ciclo tienen retroalimentación final registrada."
                : String.format(
                        "%d evaluado(s) aún no tienen retroalimentación final registrada. "
                        + "El cierre del ciclo quedará bloqueado hasta regularizarlos.",
                        total);
        return new AlertaEvaluacionesSinNotificarResponse(
                total,
                sinNotificar,
                total > 0,
                mensaje,
                "RPE 068-2020-SERVIR-PE Art. 33-39"
        );
    }

    @Transactional(readOnly = true)
    public List<InformeCierreConsolidadoResponse> listarHistorial() {
        ActiveCycle cycle = requireActiveCycle();
        return informeRepository.findByCycleIdOrderByFechaGeneracionDesc(cycle.getId()).stream()
                .map(this::mapEntity)
                .toList();
    }

    @Transactional
    public InformeCierreConsolidadoResponse generar(GenerarInformeCierreRequest request, String username) {
        ActiveCycle cycle = requireActiveCycle();
        GdrInformeCierreConsolidador.InformeCierreSnapshot snap = consolidador.consolidar(cycle);
        String actor = normalizeUsername(username);
        String observaciones = request.observacionesOrh() == null ? null : request.observacionesOrh().trim();

        GdrInformeCierre informe = GdrInformeCierre.builder()
                .cycle(cycle)
                .estado(GdrInformeCierre.ESTADO_BORRADOR)
                .totalEvaluados(snap.totalEvaluados())
                .totalBuenRendimiento(snap.totalBuenRendimiento())
                .totalSujetoObservacion(snap.totalSujetoObservacion())
                .totalDesaprobado(snap.totalDesaprobado())
                .totalDistinguido(snap.totalDistinguido())
                .totalOportunidadesMejora(snap.totalOportunidadesMejora())
                .totalConfirmaciones(snap.totalConfirmaciones())
                .totalConfirmacionesResueltas(snap.totalConfirmacionesResueltas())
                .totalDocumentosFirmados(snap.totalDocumentosFirmados())
                .observacionesOrh(observaciones)
                .generadoPor(actor)
                .fechaGeneracion(LocalDateTime.now())
                .build();

        GdrInformeCierre saved = informeRepository.save(informe);
        auditTrailService.recordEvent(
                "INFORME_CIERRE_GENERADO",
                actor,
                "Informe de cierre generado para ciclo " + cycle.getCode() + " (id=" + saved.getId() + ").",
                null
        );
        return mapEntity(saved);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadPdf(Long informeId) {
        GdrInformeCierre informe = loadInforme(informeId);
        byte[] bytes = pdfExporter.exportPdf(informe);
        String filename = "informe_cierre_" + informe.getCycle().getCode() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(bytes));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> exportCsv(Long informeId, String username) {
        GdrInformeCierre informe = loadInforme(informeId);
        String csv = buildCsv(informe);
        auditTrailService.recordEvent(
                "INFORME_CIERRE_EXPORTADO",
                normalizeUsername(username),
                "Exportación CSV informe cierre id=" + informeId + ".",
                null
        );
        String filename = "informe_cierre_" + informe.getCycle().getCode() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)));
    }

    private InformeCierreAlertaResponse buildAlertaVal06(ActiveCycle cycle) {
        LocalDate limite = validacionNormativaService.calcularFechaLimiteInforme(cycle);
        if (limite == null) {
            return new InformeCierreAlertaResponse(
                    null, null, false, "INDEFINIDO",
                    "Configure la fecha de inicio del ciclo para calcular el plazo del informe de cierre.",
                    REF_VAL06
            );
        }
        LocalDate hoy = LocalDate.now();
        boolean vencida = hoy.isAfter(limite);
        Integer diasRestantes = vencida ? 0 : (int) ChronoUnit.DAYS.between(hoy, limite);
        String nivel = resolveSemaforo(diasRestantes, vencida);
        String mensaje = vencida
                ? "El plazo para remitir el informe de cierre venció el " + limite + ". Regularice de inmediato."
                : "Quedan " + diasRestantes + " día(s) calendario para remitir el informe de cierre (límite: " + limite + ").";
        return new InformeCierreAlertaResponse(limite, diasRestantes, vencida, nivel, mensaje, REF_VAL06);
    }

    private String resolveSemaforo(Integer diasRestantes, boolean vencida) {
        if (vencida || diasRestantes == null || diasRestantes <= 7) {
            return "CRITICO";
        }
        if (diasRestantes <= 30) {
            return "ADVERTENCIA";
        }
        return "OK";
    }

    private String buildCsv(GdrInformeCierre informe) {
        return String.join(",",
                quote("ciclo"), quote(informe.getCycle().getName()),
                quote("estado"), quote(informe.getEstado()),
                quote("totalEvaluados"), String.valueOf(informe.getTotalEvaluados()),
                quote("buenRendimiento"), String.valueOf(informe.getTotalBuenRendimiento()),
                quote("sujetoObservacion"), String.valueOf(informe.getTotalSujetoObservacion()),
                quote("desaprobado"), String.valueOf(informe.getTotalDesaprobado()),
                quote("distinguido"), String.valueOf(informe.getTotalDistinguido()),
                quote("oportunidadesMejora"), String.valueOf(informe.getTotalOportunidadesMejora()),
                quote("confirmaciones"), String.valueOf(informe.getTotalConfirmaciones()),
                quote("confirmacionesResueltas"), String.valueOf(informe.getTotalConfirmacionesResueltas()),
                quote("documentosFirmados"), String.valueOf(informe.getTotalDocumentosFirmados()),
                quote("generadoPor"), quote(informe.getGeneradoPor()),
                quote("fechaGeneracion"), quote(informe.getFechaGeneracion().toString())
        ) + "\n";
    }

    private String quote(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    private InformeCierreConsolidadoResponse mapEntity(GdrInformeCierre informe) {
        return new InformeCierreConsolidadoResponse(
                informe.getId(),
                informe.getCycle().getId(),
                informe.getCycle().getName(),
                informe.getEstado(),
                estadoLabel(informe.getEstado()),
                informe.getTotalEvaluados(),
                informe.getTotalBuenRendimiento(),
                informe.getTotalSujetoObservacion(),
                informe.getTotalDesaprobado(),
                informe.getTotalDistinguido(),
                informe.getTotalOportunidadesMejora(),
                informe.getTotalConfirmaciones(),
                informe.getTotalConfirmacionesResueltas(),
                informe.getTotalDocumentosFirmados(),
                informe.getObservacionesOrh(),
                informe.getGeneradoPor(),
                informe.getFechaGeneracion()
        );
    }

    private InformeCierreConsolidadoResponse mapSnapshot(
            ActiveCycle cycle,
            Long informeId,
            String estado,
            GdrInformeCierreConsolidador.InformeCierreSnapshot snap,
            String observaciones,
            String generadoPor,
            LocalDateTime fecha
    ) {
        return new InformeCierreConsolidadoResponse(
                informeId,
                cycle.getId(),
                cycle.getName(),
                estado,
                estadoLabel(estado),
                snap.totalEvaluados(),
                snap.totalBuenRendimiento(),
                snap.totalSujetoObservacion(),
                snap.totalDesaprobado(),
                snap.totalDistinguido(),
                snap.totalOportunidadesMejora(),
                snap.totalConfirmaciones(),
                snap.totalConfirmacionesResueltas(),
                snap.totalDocumentosFirmados(),
                observaciones,
                generadoPor,
                fecha
        );
    }

    private String estadoLabel(String estado) {
        if (GdrInformeCierre.ESTADO_VALIDADO.equals(estado)) {
            return "Validado";
        }
        if (GdrInformeCierre.ESTADO_REMITIDO.equals(estado)) {
            return "Remitido";
        }
        return "Borrador";
    }

    private GdrInformeCierre loadInforme(Long informeId) {
        return informeRepository.findById(informeId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el informe de cierre solicitado."));
    }

    private ActiveCycle requireActiveCycle() {
        return activeCycleRepository.findActiveCycle()
                .orElseThrow(() -> new DomainException("No existe un ciclo activo para consolidar el informe de cierre."));
    }

    private String normalizeUsername(String username) {
        return username == null || username.isBlank() ? "sistema-gdr" : username.trim();
    }
}
