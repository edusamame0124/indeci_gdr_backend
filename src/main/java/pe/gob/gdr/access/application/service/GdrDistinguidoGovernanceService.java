package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.ActualizarRequisitosDistinguidoRequest;
import pe.gob.gdr.access.application.dto.request.AsignarDistinguidoRequest;
import pe.gob.gdr.access.application.dto.response.AsignarDistinguidoResultResponse;
import pe.gob.gdr.access.application.dto.response.DistinguidoCandidatoFilaResponse;
import pe.gob.gdr.access.application.dto.response.DistinguidoCandidatosResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.policy.DistinguishedPerformanceQuotaPolicy;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

@Service
public class GdrDistinguidoGovernanceService {

    private final GdrResultRepository resultRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrResultService resultService;
    private final GdrValidacionNormativaService validacionNormativaService;
    private final ActaJuntaDistinguidoPdfExporter actaJuntaPdfExporter;

    public GdrDistinguidoGovernanceService(
            GdrResultRepository resultRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrResultService resultService,
            GdrValidacionNormativaService validacionNormativaService,
            ActaJuntaDistinguidoPdfExporter actaJuntaPdfExporter
    ) {
        this.resultRepository = resultRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.resultService = resultService;
        this.validacionNormativaService = validacionNormativaService;
        this.actaJuntaPdfExporter = actaJuntaPdfExporter;
    }

    @Transactional(readOnly = true)
    public DistinguidoCandidatosResponse listCandidatos(Long cycleId) {
        List<GdrResult> results = resultRepository.findAllByCycleId(cycleId);
        int notifiedUniverseTotal = (int) results.stream().filter(this::isNotified).count();
        int maxSlots = DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(notifiedUniverseTotal);
        long currentDistinguidos = results.stream().filter(this::isDistinguido).count();
        int remaining = Math.max(0, maxSlots - (int) currentDistinguidos);

        List<GdrResult> eligibleOrdered = results.stream()
                .filter(this::eligibleForDistinguidoPool)
                .sorted(Comparator.comparing(GdrResult::getConsolidatedScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(r -> r.getAssignment().getEvaluatedPerson().getId()))
                .toList();

        List<DistinguidoCandidatoFilaResponse> rows = new ArrayList<>();
        int rankCounter = 1;
        for (GdrResult eligible : eligibleOrdered) {
            rows.add(mapRow(eligible, rankCounter++));
        }

        List<GdrResult> remainder = results.stream()
                .filter(r -> !eligibleForDistinguidoPool(r))
                .sorted(Comparator.comparing(GdrResult::getConsolidatedScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(r -> r.getAssignment().getEvaluatedPerson().getId()))
                .toList();
        for (GdrResult rest : remainder) {
            rows.add(mapRow(rest, 0));
        }

        int pendientes = (int) rows.stream().filter(DistinguidoCandidatoFilaResponse::confirmacionPendiente).count();
        boolean bloqueoVal08 = pendientes > 0;

        return new DistinguidoCandidatosResponse(
                notifiedUniverseTotal,
                maxSlots,
                (int) currentDistinguidos,
                remaining,
                pendientes,
                bloqueoVal08,
                rows
        );
    }

    @Transactional
    public void actualizarRequisitos(Long assignmentId, ActualizarRequisitosDistinguidoRequest request, Long cycleId) {
        GdrResult result = resultRepository.findByAssignmentIdAndCycle(assignmentId, cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe resultado activo para la asignacion indicada."));
        result.setQualRatingNotified(flagToChar(request.qualRatingNotified()));
        result.setDirective82Compliance(flagToChar(request.directive82ComplianceConfirmed()));
        resultRepository.save(result);
    }

    @Transactional
    public AsignarDistinguidoResultResponse asignar(AsignarDistinguidoRequest request, Long cycleId) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(request.finalEvaluationIds());
        if (uniqueIds.isEmpty()) {
            throw new DomainException("La solicitud no contiene evaluaciones finales.");
        }
        List<GdrResult> all = resultRepository.findAllByCycleId(cycleId);
        int notifiedUniverseTotal = (int) all.stream().filter(this::isNotified).count();
        int maxSlots = DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(notifiedUniverseTotal);
        long currentDistinguidos = all.stream().filter(this::isDistinguido).count();
        int remaining = Math.max(0, maxSlots - (int) currentDistinguidos);

        if (uniqueIds.size() > remaining) {
            throw new DomainException(
                    "El cupo Rendimiento distinguido no permite asignar tantas evaluaciones en este ciclo (restantes: "
                            + remaining + ")."
            );
        }

        int assigned = 0;
        for (Long evaluationId : uniqueIds) {
            GdrFinalEvaluation evaluation = finalEvaluationRepository.findByIdAndCycle(evaluationId, cycleId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Evaluacion final no encontrada o fuera del ciclo indicado (id " + evaluationId + ")."));
            GdrResult result = resultRepository.findByAssignmentIdAndCycle(evaluation.getAssignment().getId(), cycleId)
                    .orElseThrow(() -> new DomainException("El resultado ligado a la evaluacion no existe."));
            validacionNormativaService.validarSinConfirmacionPendienteParaDistinguido(
                    result.getAssignment().getEvaluatedPerson().getDisplayName(),
                    result.getEstadoConfirmacion());
            if (!eligibleForJuntaUpgrade(result)) {
                throw new DomainException(
                        "La evaluacion " + evaluationId + " no elegible para distinguido (requiere BUEN rendimiento,"
                                + " calificacion notificada y observancia 8.2 confirmadas por ORH)."
                );
            }
            evaluation.setQualitativeRatingCode(QualitativeRating.DISTINGUIDO.code());
            finalEvaluationRepository.save(evaluation);

            BigDecimal score = Objects.requireNonNullElse(result.getConsolidatedScore(), BigDecimal.ZERO);
            resultService.syncResult(evaluation.getAssignment(), evaluation, score, QualitativeRating.DISTINGUIDO.code());
            assigned++;
        }

        List<GdrResult> refreshed = resultRepository.findAllByCycleId(cycleId);
        long after = refreshed.stream().filter(this::isDistinguido).count();
        int remainingAfter = Math.max(
                0,
                DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(
                        (int) refreshed.stream().filter(this::isNotified).count()
                ) - (int) after
        );
        return new AsignarDistinguidoResultResponse(assigned, remainingAfter);
    }

    /** P6-05 — Acta de la Junta con servidores distinguidos del ciclo indicado. */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadActaJuntaPdf(Long cycleId) {
        List<GdrResult> distinguidos = resultRepository.findAllByCycleId(cycleId).stream()
                .filter(this::isDistinguido)
                .toList();
        String cicloNombre = distinguidos.isEmpty()
                ? "Ciclo " + cycleId
                : distinguidos.get(0).getAssignment().getCycle().getName();
        byte[] bytes = actaJuntaPdfExporter.exportPdf(distinguidos, cicloNombre);
        Resource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"acta_junta_distinguido.pdf\"")
                .body(resource);
    }

    private DistinguidoCandidatoFilaResponse mapRow(GdrResult result, int rankIfEligible) {
        String qr = result.getQualitativeRatingCode();
        boolean already = isDistinguido(result);
        boolean eligible = eligibleForDistinguidoPool(result);
        boolean pendiente = isConfirmacionPendiente(result);

        return new DistinguidoCandidatoFilaResponse(
                result.getAssignment().getId(),
                result.getFinalEvaluation().getId(),
                result.getAssignment().getEvaluatedPerson().getId(),
                result.getAssignment().getEvaluatedPerson().getDisplayName(),
                result.getConsolidatedScore(),
                qr,
                QualitativeRating.labelOf(qr),
                isNotified(result),
                isDirective82Ok(result),
                eligible,
                eligible ? rankIfEligible : 0,
                already,
                result.getEstadoConfirmacion(),
                estadoConfirmacionLabel(result.getEstadoConfirmacion()),
                pendiente,
                pendiente
        );
    }

    private String estadoConfirmacionLabel(String estado) {
        if (GdrResult.ESTADO_CONF_PENDIENTE.equals(estado)) {
            return "Confirmación pendiente (CIE)";
        }
        if (GdrResult.ESTADO_CONF_RESUELTA.equals(estado)) {
            return "Confirmación resuelta";
        }
        return "Sin solicitud de confirmación";
    }

    private boolean isConfirmacionPendiente(GdrResult result) {
        return GdrResult.ESTADO_CONF_PENDIENTE.equals(result.getEstadoConfirmacion());
    }

    private boolean eligibleForDistinguidoPool(GdrResult result) {
        return eligibleForJuntaUpgrade(result);
    }

    private boolean eligibleForJuntaUpgrade(GdrResult result) {
        String q = result.getQualitativeRatingCode();
        return QualitativeRating.BUEN_RENDIMIENTO.code().equals(q)
                && isNotified(result)
                && isDirective82Ok(result);
    }

    private boolean isDistinguido(GdrResult result) {
        return QualitativeRating.DISTINGUIDO.code().equals(result.getQualitativeRatingCode());
    }

    private boolean isNotified(GdrResult result) {
        return "Y".equalsIgnoreCase(result.getQualRatingNotified());
    }

    private boolean isDirective82Ok(GdrResult result) {
        return "Y".equalsIgnoreCase(result.getDirective82Compliance());
    }

    private String flagToChar(boolean flag) {
        return flag ? "Y" : "N";
    }
}
