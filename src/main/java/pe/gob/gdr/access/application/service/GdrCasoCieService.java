package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.ResolverCasoCieRequest;
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.mapper.GdrConfirmacionMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * Bandeja y resolución de casos del Comité Institucional de Evaluación.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 42 (la decisión del CIE es definitiva).
 */
@Service
public class GdrCasoCieService {

    private static final String DOC_TYPE_ACTA_CIE = "ACTA_REUNION";
    private static final String REF_NORMATIVA_ACTA_CIE = "RPE 068-2020-SERVIR-PE Art. 42";

    private final GdrCasoCieRepository casoCieRepository;
    private final GdrSolicitudConfirmacionRepository solicitudRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrPublicHolidayRepository publicHolidayRepository;
    private final GdrValidacionNormativaService validacionNormativaService;
    private final GdrResultService resultService;
    private final GdrConfirmacionMapper mapper;
    private final NotificacionesService notificacionesService;
    private final UserRepository userRepository;
    private final GdrResultConfirmacionSyncService resultConfirmacionSyncService;
    private final GdrResultRepository resultRepository;
    private final ActaCiePdfExporter actaCiePdfExporter;
    private final DocumentManagementService documentManagementService;

    public GdrCasoCieService(
            GdrCasoCieRepository casoCieRepository,
            GdrSolicitudConfirmacionRepository solicitudRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrPublicHolidayRepository publicHolidayRepository,
            GdrValidacionNormativaService validacionNormativaService,
            GdrResultService resultService,
            GdrConfirmacionMapper mapper,
            NotificacionesService notificacionesService,
            UserRepository userRepository,
            GdrResultConfirmacionSyncService resultConfirmacionSyncService,
            GdrResultRepository resultRepository,
            ActaCiePdfExporter actaCiePdfExporter,
            DocumentManagementService documentManagementService
    ) {
        this.casoCieRepository = casoCieRepository;
        this.solicitudRepository = solicitudRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.validacionNormativaService = validacionNormativaService;
        this.resultService = resultService;
        this.mapper = mapper;
        this.notificacionesService = notificacionesService;
        this.userRepository = userRepository;
        this.resultConfirmacionSyncService = resultConfirmacionSyncService;
        this.resultRepository = resultRepository;
        this.actaCiePdfExporter = actaCiePdfExporter;
        this.documentManagementService = documentManagementService;
    }

    /** Bandeja de casos CIE con semáforo de plazo de convocatoria (VAL-05, alerta). */
    @Transactional(readOnly = true)
    public List<CasoCieResponse> listarBandeja() {
        return casoCieRepository.findAllOrderByFechaIngreso().stream()
                .map(this::mapConSemaforo)
                .toList();
    }

    @Transactional(readOnly = true)
    public CasoCieResponse getDetalle(Long casoId) {
        return mapConSemaforo(loadCaso(casoId));
    }

    /**
     * P4 — Registra la decisión definitiva del CIE. CONFIRMA mantiene la
     * calificación; MODIFICA la reemplaza y sincroniza el resultado consolidado.
     * CONSTRAINT: no modifica la lógica de cálculo de la evaluación.
     */
    @Transactional
    public CasoCieResponse resolver(Long casoId, ResolverCasoCieRequest request, String username) {
        GdrCasoCie caso = loadCaso(casoId);
        validarCasoPendiente(caso);

        if (GdrCasoCie.DECISION_MODIFICA.equals(request.decision())) {
            aplicarModificacionCalificacion(caso, normalizarCalificacion(request.calificacionResultado()));
        }

        caso.setDecision(request.decision());
        caso.setSustentoCie(request.sustentoCie().trim());
        caso.setFechaDecision(LocalDateTime.now());
        caso.setEstado(GdrCasoCie.ESTADO_RESUELTO);
        GdrCasoCie resuelto = casoCieRepository.save(caso);

        cerrarSolicitud(resuelto.getSolicitud());
        resultConfirmacionSyncService.marcarResuelta(resuelto.getSolicitud().getFinalEvaluation());
        persistirActaCie(resuelto, username);
        notificarResolucionAlEvaluado(resuelto);
        return mapConSemaforo(resuelto);
    }

    /** P6-04 — Descarga del acta CIE (documento persistido o generación bajo demanda). */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadActaCiePdf(Long casoId) {
        GdrCasoCie caso = loadCaso(casoId);
        if (!GdrCasoCie.ESTADO_RESUELTO.equals(caso.getEstado())) {
            throw new DomainException(
                    "El acta del CIE estará disponible cuando el caso quede resuelto.");
        }
        String filename = "acta_cie_" + caso.getNumeroCaso() + ".pdf";
        if (caso.getActaDocId() != null) {
            return documentManagementService.downloadStoredPdf(caso.getActaDocId(), filename);
        }
        byte[] bytes = actaCiePdfExporter.exportPdf(caso);
        Resource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void validarCasoPendiente(GdrCasoCie caso) {
        if (GdrCasoCie.ESTADO_RESUELTO.equals(caso.getEstado())) {
            throw new DomainException(
                    "El caso ya fue resuelto por el CIE. La decisión es definitiva y no puede modificarse. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 42.");
        }
    }

    private String normalizarCalificacion(String calificacionResultado) {
        if (calificacionResultado == null || calificacionResultado.isBlank()) {
            throw new DomainException(
                    "La decisión MODIFICA requiere indicar la nueva calificación resultante.");
        }
        String code = calificacionResultado.trim().toUpperCase();
        if (QualitativeRating.labelOf(code) == null) {
            throw new DomainException("La calificación indicada no es válida: " + calificacionResultado + ".");
        }
        return code;
    }

    private void aplicarModificacionCalificacion(GdrCasoCie caso, String nuevaCalificacion) {
        GdrFinalEvaluation evaluation = caso.getSolicitud().getFinalEvaluation();
        evaluation.setQualitativeRatingCode(nuevaCalificacion);
        GdrFinalEvaluation saved = finalEvaluationRepository.save(evaluation);
        resultService.syncResult(
                saved.getAssignment(), saved, saved.getConsolidatedScore(), nuevaCalificacion);
        caso.setCalificacionResultado(nuevaCalificacion);
    }

    private void cerrarSolicitud(GdrSolicitudConfirmacion solicitud) {
        solicitud.setEstado(GdrSolicitudConfirmacion.ESTADO_RESUELTA);
        solicitudRepository.save(solicitud);
    }

    /** Notifica la decisión al evaluado; el flujo principal no se bloquea si falla. */
    private void notificarResolucionAlEvaluado(GdrCasoCie caso) {
        Long evaluadoPersonId = caso.getSolicitud().getEvaluado().getId();
        userRepository.findActiveGdrUsersByPersonId(evaluadoPersonId).forEach(user ->
                notificacionesService.emitForUser(
                        user.getUsername(),
                        NotificacionesService.CONFIRMACION_RESUELTA,
                        caso.getNumeroCaso()));
    }

    private CasoCieResponse mapConSemaforo(GdrCasoCie caso) {
        LocalDate plazo = caso.getPlazoConvocatoria();
        Integer diasRestantes = null;
        boolean vencida = false;
        if (plazo != null && GdrCasoCie.ESTADO_RECIBIDO.equals(caso.getEstado())) {
            LocalDate hoy = LocalDate.now();
            Set<LocalDate> feriados = hoy.isAfter(plazo)
                    ? Set.of()
                    : publicHolidayRepository.findHolidayDatesBetween(hoy, plazo);
            diasRestantes = validacionNormativaService.contarDiasHabilesRestantes(hoy, plazo, feriados);
            vencida = hoy.isAfter(plazo);
        }
        return mapper.toCasoResponse(caso, diasRestantes, vencida);
    }

    private void persistirActaCie(GdrCasoCie caso, String username) {
        if (caso.getActaDocId() != null) {
            return;
        }
        GdrEvaluationAssignment assignment = caso.getSolicitud().getFinalEvaluation().getAssignment();
        GdrResult result = resultRepository.findByAssignmentIdInActiveCycle(assignment.getId())
                .orElseThrow(() -> new DomainException(
                        "No existe resultado consolidado para persistir el acta del CIE."));
        byte[] pdf = actaCiePdfExporter.exportPdf(caso);
        String actor = username == null || username.isBlank() ? "sistema-gdr" : username.trim();
        Long docId = documentManagementService.persistGeneratedActaDocument(
                result,
                DOC_TYPE_ACTA_CIE,
                pdf,
                "acta_cie_" + caso.getNumeroCaso() + ".pdf",
                REF_NORMATIVA_ACTA_CIE,
                actor
        );
        caso.setActaDocId(docId);
        casoCieRepository.save(caso);
    }

    private GdrCasoCie loadCaso(Long casoId) {
        return casoCieRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el caso CIE solicitado."));
    }
}
