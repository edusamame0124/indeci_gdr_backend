package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.dto.response.ReporteAvanceResponse;
import pe.gob.gdr.access.application.dto.response.ReporteDistribucionCalificacionResponse;
import pe.gob.gdr.access.application.dto.response.ReporteOportunidadMejoraResponse;
import pe.gob.gdr.access.application.dto.response.ReporteResultadoResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
import pe.gob.gdr.access.application.mapper.GdrConfirmacionMapper;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.DocSignedFileRepository;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementFollowupRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;

@Service
public class ReportesService {

    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrResultRepository resultRepository;
    private final DocSignedFileRepository signedFileRepository;
    private final GdrImprovementOpportunityRepository improvementOpportunityRepository;
    private final GdrImprovementFollowupRepository improvementFollowupRepository;
    private final GdrSolicitudConfirmacionRepository solicitudConfirmacionRepository;
    private final GdrCasoCieRepository casoCieRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final GdrConfirmacionMapper confirmacionMapper;
    private final AuditTrailService auditTrailService;

    public ReportesService(
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrResultRepository resultRepository,
            DocSignedFileRepository signedFileRepository,
            GdrImprovementOpportunityRepository improvementOpportunityRepository,
            GdrImprovementFollowupRepository improvementFollowupRepository,
            GdrSolicitudConfirmacionRepository solicitudConfirmacionRepository,
            GdrCasoCieRepository casoCieRepository,
            ActiveCycleRepository activeCycleRepository,
            GdrConfirmacionMapper confirmacionMapper,
            AuditTrailService auditTrailService
    ) {
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.resultRepository = resultRepository;
        this.signedFileRepository = signedFileRepository;
        this.improvementOpportunityRepository = improvementOpportunityRepository;
        this.improvementFollowupRepository = improvementFollowupRepository;
        this.solicitudConfirmacionRepository = solicitudConfirmacionRepository;
        this.casoCieRepository = casoCieRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.confirmacionMapper = confirmacionMapper;
        this.auditTrailService = auditTrailService;
    }

    public List<ReporteAvanceResponse> getProgressReport(Long evaluatedId, Long cycleId) {
        List<GdrGoal> goals = goalRepository.findActiveGoalsByCycle(cycleId);
        List<GdrEvidence> evidences = evidenceRepository.findActiveByCycle(cycleId);
        List<GdrResult> results = resultRepository.findAllByCycleId(cycleId);
        List<DocSignedFile> signedFiles = signedFileRepository.findAllByCycleId(cycleId);
        List<GdrImprovementOpportunity> opportunities = improvementOpportunityRepository.findAllByCycleId(cycleId);

        Map<Long, List<GdrGoal>> goalsByAssignment = groupByAssignment(goals);
        Map<Long, Integer> evidenceCountByAssignment = new HashMap<>();
        Map<Long, Integer> goalsWithEvidenceByAssignment = new HashMap<>();
        for (GdrEvidence evidence : evidences) {
            Long assignmentId = evidence.getGoal().getAssignment().getId();
            evidenceCountByAssignment.merge(assignmentId, 1, Integer::sum);
            goalsWithEvidenceByAssignment.putIfAbsent(assignmentId, 0);
        }
        Map<Long, java.util.Set<Long>> goalIdsWithEvidenceByAssignment = new HashMap<>();
        for (GdrEvidence evidence : evidences) {
            goalIdsWithEvidenceByAssignment
                    .computeIfAbsent(evidence.getGoal().getAssignment().getId(), ignored -> new java.util.LinkedHashSet<>())
                    .add(evidence.getGoal().getId());
        }
        goalIdsWithEvidenceByAssignment.forEach(
                (assignmentId, goalIds) -> goalsWithEvidenceByAssignment.put(assignmentId, goalIds.size())
        );

        Map<Long, GdrResult> resultsByAssignment = new HashMap<>();
        for (GdrResult result : results) {
            resultsByAssignment.put(result.getAssignment().getId(), result);
        }
        Map<Long, Integer> documentCountByResult = countDocumentsByResult(signedFiles);
        Map<Long, Map<String, Integer>> improvementStatsByResult = countImprovementsByResult(opportunities);

        return goals.stream()
                .map(goal -> goal.getAssignment())
                .collect(java.util.stream.Collectors.toMap(
                        assignment -> assignment.getId(),
                        assignment -> assignment,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .filter(assignment -> evaluatedId == null || assignment.getEvaluatedPerson().getId().equals(evaluatedId))
                .map(assignment -> {
                    GdrResult result = resultsByAssignment.get(assignment.getId());
                    Map<String, Integer> improvementStats = result == null
                            ? Map.of("OPEN", 0, "CLOSED", 0)
                            : improvementStatsByResult.getOrDefault(result.getId(), Map.of("OPEN", 0, "CLOSED", 0));
                    return new ReporteAvanceResponse(
                            assignment.getId(),
                            assignment.getEvaluatedPerson().getId(),
                            assignment.getEvaluatedPerson().getDisplayName(),
                            assignment.getEvaluatorPerson().getDisplayName(),
                            assignment.getCycle().getName(),
                            goalsByAssignment.getOrDefault(assignment.getId(), List.of()).size(),
                            evidenceCountByAssignment.getOrDefault(assignment.getId(), 0),
                            goalsWithEvidenceByAssignment.getOrDefault(assignment.getId(), 0),
                            result != null && result.getFinalEvaluation() != null,
                            result != null,
                            result == null ? 0 : documentCountByResult.getOrDefault(result.getId(), 0),
                            improvementStats.getOrDefault("OPEN", 0),
                            improvementStats.getOrDefault("CLOSED", 0)
                    );
                })
                .toList();
    }

    public List<ReporteResultadoResponse> getResultsReport(Long evaluatedId, Long cycleId) {
        Map<Long, Integer> documentCountByResult = countDocumentsByResult(signedFileRepository.findAllByCycleId(cycleId));
        Map<Long, Integer> improvementCountByResult = new HashMap<>();
        for (GdrImprovementOpportunity opportunity : improvementOpportunityRepository.findAllByCycleId(cycleId)) {
            improvementCountByResult.merge(opportunity.getResult().getId(), 1, Integer::sum);
        }

        return resultRepository.findAllByCycleId(cycleId).stream()
                .filter(result -> evaluatedId == null || result.getAssignment().getEvaluatedPerson().getId().equals(evaluatedId))
                .map(result -> new ReporteResultadoResponse(
                        result.getId(),
                        result.getAssignment().getEvaluatedPerson().getId(),
                        result.getAssignment().getEvaluatedPerson().getDisplayName(),
                        result.getAssignment().getEvaluatorPerson().getDisplayName(),
                        result.getAssignment().getCycle().getName(),
                        safeScore(result.getFinalEvaluation().getConsolidatedScore()),
                        safeScore(result.getConsolidatedScore()),
                        result.getFinalEvaluation().getEvaluationComment(),
                        documentCountByResult.getOrDefault(result.getId(), 0),
                        improvementCountByResult.getOrDefault(result.getId(), 0)
                ))
                .toList();
    }

    public List<ReporteOportunidadMejoraResponse> getImprovementReport(Long evaluatedId, String estadoCodigo, Long cycleId) {
        String normalizedState = estadoCodigo == null ? null : estadoCodigo.trim().toUpperCase(Locale.ROOT);
        return improvementOpportunityRepository.findAllByCycleId(cycleId).stream()
                .filter(opportunity -> evaluatedId == null
                        || opportunity.getResult().getAssignment().getEvaluatedPerson().getId().equals(evaluatedId))
                .filter(opportunity -> normalizedState == null
                        || opportunity.getImprovementStatus().getCode().equalsIgnoreCase(normalizedState))
                .map(opportunity -> new ReporteOportunidadMejoraResponse(
                        opportunity.getId(),
                        opportunity.getResult().getId(),
                        opportunity.getResult().getAssignment().getEvaluatedPerson().getId(),
                        opportunity.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                        opportunity.getResult().getAssignment().getEvaluatorPerson().getDisplayName(),
                        opportunity.getResult().getAssignment().getCycle().getName(),
                        opportunity.getImprovementStatus().getCode(),
                        opportunity.getImprovementStatus().getName(),
                        opportunity.getResponsible(),
                        opportunity.getTargetDate(),
                        opportunity.getCreatedAt(),
                        opportunity.getClosedAt(),
                        improvementFollowupRepository.findByOpportunityId(opportunity.getId()).size()
                ))
                .toList();
    }

    public byte[] exportProgressCsv(List<ReporteAvanceResponse> rows, String username) {
        auditTrailService.recordEvent("REPORTE_AVANCE_EXPORTADO", username, "Se exporto el reporte de avance.", null);
        return toCsv(
                List.of("idAsignacion", "idEvaluado", "evaluado", "evaluador", "ciclo", "totalMetas", "totalEvidencias",
                        "metasConEvidencia", "evaluacionFinalDisponible", "resultadoDisponible", "totalDocumentosFirmados",
                        "oportunidadesAbiertas", "oportunidadesCerradas"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.idAsignacion()),
                                String.valueOf(row.idEvaluado()),
                                row.evaluado(),
                                row.evaluador(),
                                row.ciclo(),
                                String.valueOf(row.totalMetas()),
                                String.valueOf(row.totalEvidencias()),
                                String.valueOf(row.metasConEvidencia()),
                                String.valueOf(row.evaluacionFinalDisponible()),
                                String.valueOf(row.resultadoDisponible()),
                                String.valueOf(row.totalDocumentosFirmados()),
                                String.valueOf(row.oportunidadesAbiertas()),
                                String.valueOf(row.oportunidadesCerradas())
                        ))
                        .toList()
        );
    }

    public byte[] exportResultsCsv(List<ReporteResultadoResponse> rows, String username) {
        auditTrailService.recordEvent("REPORTE_RESULTADOS_EXPORTADO", username, "Se exporto el reporte de resultados.", null);
        return toCsv(
                List.of("idResultado", "idEvaluado", "evaluado", "evaluador", "ciclo", "puntajeFinal",
                        "puntajeResultado", "comentarioEvaluacion", "documentosFirmados", "oportunidadesMejora"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.idResultado()),
                                String.valueOf(row.idEvaluado()),
                                row.evaluado(),
                                row.evaluador(),
                                row.ciclo(),
                                row.puntajeFinal().toPlainString(),
                                row.puntajeResultado().toPlainString(),
                                row.comentarioEvaluacion() == null ? "" : row.comentarioEvaluacion(),
                                String.valueOf(row.documentosFirmados()),
                                String.valueOf(row.oportunidadesMejora())
                        ))
                        .toList()
        );
    }

    public List<SolicitudConfirmacionResponse> getConfirmacionesReport() {
        ActiveCycle cycle = requireActiveCycle();
        return solicitudConfirmacionRepository.findByCycleIdOrderByFechaSolicitud(cycle.getId()).stream()
                .map(solicitud -> confirmacionMapper.toSolicitudResponse(
                        solicitud,
                        casoCieRepository.findBySolicitudId(solicitud.getId()).orElse(null)
                ))
                .toList();
    }

    public List<CasoCieResponse> getCieReport() {
        return casoCieRepository.findAllOrderByFechaIngreso().stream()
                .map(caso -> confirmacionMapper.toCasoResponse(caso, null, false))
                .toList();
    }

    public List<ReporteDistribucionCalificacionResponse> getDistribucionCalificacionesReport(Long cycleId) {
        List<GdrResult> results = resultRepository.findAllByCycleId(cycleId);
        if (results.isEmpty()) {
            return List.of();
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (GdrResult result : results) {
            String code = result.getQualitativeRatingCode() == null
                    ? QualitativeRating.NO_CALIFICABLE.code()
                    : result.getQualitativeRatingCode();
            counts.merge(code, 1, Integer::sum);
        }
        int total = results.size();
        return counts.entrySet().stream()
                .map(entry -> new ReporteDistribucionCalificacionResponse(
                        entry.getKey(),
                        QualitativeRating.labelOf(entry.getKey()),
                        entry.getValue(),
                        BigDecimal.valueOf(entry.getValue() * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();
    }

    public byte[] exportConfirmacionesCsv(List<SolicitudConfirmacionResponse> rows, String username) {
        auditTrailService.recordEvent("REPORTE_CONFIRMACIONES_EXPORTADO", username, "Se exportó el reporte de confirmaciones.", null);
        return toCsv(
                List.of("id", "evaluado", "ciclo", "estado", "numeroCaso", "decision", "calificacionResultado", "fechaSolicitud"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.id()),
                                row.evaluadoNombre(),
                                row.cicloNombre(),
                                row.estadoLabel(),
                                row.numeroCaso() == null ? "" : row.numeroCaso(),
                                row.decision() == null ? "" : row.decision(),
                                row.calificacionResultadoLabel() == null ? "" : row.calificacionResultadoLabel(),
                                row.fechaSolicitud() == null ? "" : row.fechaSolicitud().toString()
                        ))
                        .toList()
        );
    }

    public byte[] exportCieCsv(List<CasoCieResponse> rows, String username) {
        auditTrailService.recordEvent("REPORTE_CIE_EXPORTADO", username, "Se exportó el reporte de casos CIE.", null);
        return toCsv(
                List.of("id", "numeroCaso", "evaluado", "evaluador", "estado", "decision", "calificacionResultado", "fechaIngresoCie"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.id()),
                                row.numeroCaso(),
                                row.evaluadoNombre(),
                                row.evaluadorNombre(),
                                row.estadoLabel(),
                                row.decision() == null ? "" : row.decision(),
                                row.calificacionResultadoLabel() == null ? "" : row.calificacionResultadoLabel(),
                                row.fechaIngresoCie() == null ? "" : row.fechaIngresoCie().toString()
                        ))
                        .toList()
        );
    }

    public byte[] exportDistribucionCsv(List<ReporteDistribucionCalificacionResponse> rows, String username) {
        auditTrailService.recordEvent(
                "REPORTE_DISTRIBUCION_EXPORTADO",
                username,
                "Se exportó el reporte de distribución de calificaciones.",
                null
        );
        return toCsv(
                List.of("codigoCalificacion", "etiquetaCalificacion", "cantidad", "porcentaje"),
                rows.stream()
                        .map(row -> List.of(
                                row.codigoCalificacion(),
                                row.etiquetaCalificacion() == null ? "" : row.etiquetaCalificacion(),
                                String.valueOf(row.cantidad()),
                                row.porcentaje().toPlainString()
                        ))
                        .toList()
        );
    }

    public byte[] exportImprovementCsv(List<ReporteOportunidadMejoraResponse> rows, String username) {
        auditTrailService.recordEvent(
                "REPORTE_OPORTUNIDADES_EXPORTADO",
                username,
                "Se exporto el reporte de oportunidades de mejora.",
                null
        );
        return toCsv(
                List.of("idOportunidadMejora", "idResultado", "idEvaluado", "evaluado", "evaluador", "ciclo",
                        "estadoCodigo", "estadoNombre", "responsable", "plazoCompromiso", "fechaRegistro",
                        "fechaCierre", "totalSeguimientos"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.idOportunidadMejora()),
                                String.valueOf(row.idResultado()),
                                String.valueOf(row.idEvaluado()),
                                row.evaluado(),
                                row.evaluador(),
                                row.ciclo(),
                                row.estadoCodigo(),
                                row.estadoNombre(),
                                row.responsable(),
                                row.plazoCompromiso() == null ? "" : row.plazoCompromiso().toString(),
                                row.fechaRegistro() == null ? "" : row.fechaRegistro().toString(),
                                row.fechaCierre() == null ? "" : row.fechaCierre().toString(),
                                String.valueOf(row.totalSeguimientos())
                        ))
                        .toList()
        );
    }

    private Map<Long, List<GdrGoal>> groupByAssignment(List<GdrGoal> goals) {
        Map<Long, List<GdrGoal>> grouped = new HashMap<>();
        for (GdrGoal goal : goals) {
            grouped.computeIfAbsent(goal.getAssignment().getId(), ignored -> new ArrayList<>()).add(goal);
        }
        return grouped;
    }

    private Map<Long, Integer> countDocumentsByResult(List<DocSignedFile> signedFiles) {
        Map<Long, Integer> counts = new HashMap<>();
        for (DocSignedFile document : signedFiles) {
            counts.merge(document.getResult().getId(), 1, Integer::sum);
        }
        return counts;
    }

    private Map<Long, Map<String, Integer>> countImprovementsByResult(List<GdrImprovementOpportunity> opportunities) {
        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        for (GdrImprovementOpportunity opportunity : opportunities) {
            Map<String, Integer> perStatus = counts.computeIfAbsent(opportunity.getResult().getId(), ignored -> new HashMap<>());
            perStatus.merge(opportunity.getImprovementStatus().getCode().toUpperCase(Locale.ROOT), 1, Integer::sum);
        }
        return counts;
    }

    private BigDecimal safeScore(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private byte[] toCsv(List<String> headers, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(",", headers)).append('\n');
        for (List<String> row : rows) {
            builder.append(row.stream().map(this::escapeCsv).collect(java.util.stream.Collectors.joining(","))).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private ActiveCycle requireActiveCycle() {
        return activeCycleRepository.findActiveCycle()
                .orElseThrow(() -> new IllegalStateException("No existe un ciclo activo para generar el reporte."));
    }
}
