package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.SolicitudConfirmacionRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
import pe.gob.gdr.access.application.mapper.GdrConfirmacionMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * Solicitud de confirmación de calificación y derivación automática al CIE.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 41-42.
 */
@Service
public class GdrSolicitudConfirmacionService {

    private static final String ROL_CIE = "GDR_CIE";

    private final GdrSolicitudConfirmacionRepository solicitudRepository;
    private final GdrCasoCieRepository casoCieRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final GdrPublicHolidayRepository publicHolidayRepository;
    private final GdrValidacionNormativaService validacionNormativaService;
    private final GdrAccessPolicyService accessPolicyService;
    private final GdrConfirmacionMapper mapper;
    private final NotificacionesService notificacionesService;
    private final UserRepository userRepository;
    private final GdrResultConfirmacionSyncService resultConfirmacionSyncService;

    public GdrSolicitudConfirmacionService(
            GdrSolicitudConfirmacionRepository solicitudRepository,
            GdrCasoCieRepository casoCieRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository,
            ActiveCycleRepository activeCycleRepository,
            GdrPublicHolidayRepository publicHolidayRepository,
            GdrValidacionNormativaService validacionNormativaService,
            GdrAccessPolicyService accessPolicyService,
            GdrConfirmacionMapper mapper,
            NotificacionesService notificacionesService,
            UserRepository userRepository,
            GdrResultConfirmacionSyncService resultConfirmacionSyncService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.casoCieRepository = casoCieRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.validacionNormativaService = validacionNormativaService;
        this.accessPolicyService = accessPolicyService;
        this.mapper = mapper;
        this.notificacionesService = notificacionesService;
        this.userRepository = userRepository;
        this.resultConfirmacionSyncService = resultConfirmacionSyncService;
    }

    /**
     * P4 — Registra la solicitud de confirmación del evaluado y la deriva
     * automáticamente al CIE. VAL-04: bloqueo si está fuera del plazo de
     * 5 días hábiles (RPE 068-2020 Art. 41).
     */
    @Transactional
    public SolicitudConfirmacionResponse solicitar(SolicitudConfirmacionRequest request, String username) {
        GdrFinalEvaluation evaluation = loadEvaluation(request.finalEvaluationId());
        validarEvaluadoPropietario(username, evaluation);
        validarSolicitudUnica(evaluation.getId());
        validacionNormativaService.validarSolicitudDentroDePlazo(
                LocalDate.now(), evaluation.getPlazoSolicitudConfirmacion());

        GdrSolicitudConfirmacion solicitud = solicitudRepository.save(buildSolicitud(evaluation, request));
        GdrCasoCie caso = derivarAlCie(solicitud);
        resultConfirmacionSyncService.marcarPendiente(evaluation);
        notificarDerivacionAlCie(caso);
        return mapper.toSolicitudResponse(solicitud, caso);
    }

    /** Estado de la solicitud asociada a una evaluación final; vacío si no existe. */
    @Transactional(readOnly = true)
    public Optional<SolicitudConfirmacionResponse> findByEvaluacion(Long finalEvaluationId, String username) {
        GdrFinalEvaluation evaluation = loadEvaluation(finalEvaluationId);
        validarAccesoLectura(username, evaluation);
        return solicitudRepository.findByFinalEvaluationId(finalEvaluationId)
                .map(this::mapWithCaso);
    }

    /** Listado de solicitudes del ciclo activo — supervisión ORH/CIE. */
    @Transactional(readOnly = true)
    public List<SolicitudConfirmacionResponse> listarCicloActivo() {
        Long cycleId = activeCycleRepository.findActiveCycle()
                .orElseThrow(() -> new DomainException("No existe un ciclo GDR activo."))
                .getId();
        return solicitudRepository.findByCycleIdOrderByFechaSolicitud(cycleId).stream()
                .map(this::mapWithCaso)
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private GdrSolicitudConfirmacion buildSolicitud(GdrFinalEvaluation evaluation, SolicitudConfirmacionRequest request) {
        return GdrSolicitudConfirmacion.builder()
                .finalEvaluation(evaluation)
                .evaluado(evaluation.getAssignment().getEvaluatedPerson())
                .cycle(evaluation.getAssignment().getCycle())
                .fechaSolicitud(LocalDateTime.now())
                .sustentoEvaluado(request.sustento().trim())
                .estado(GdrSolicitudConfirmacion.ESTADO_PRESENTADA)
                .build();
    }

    private GdrCasoCie derivarAlCie(GdrSolicitudConfirmacion solicitud) {
        LocalDate hoy = LocalDate.now();
        Set<LocalDate> feriados = publicHolidayRepository.findHolidayDatesBetween(hoy, hoy.plusMonths(1));
        LocalDate plazoConvocatoria = validacionNormativaService.calcularPlazoConvocatoriaCie(hoy, feriados);

        GdrCasoCie caso = casoCieRepository.save(GdrCasoCie.builder()
                .solicitud(solicitud)
                .numeroCaso(String.format("CIE-%d-%04d", hoy.getYear(), solicitud.getId()))
                .fechaIngresoCie(LocalDateTime.now())
                .plazoConvocatoria(plazoConvocatoria)
                .estado(GdrCasoCie.ESTADO_RECIBIDO)
                .build());

        solicitud.setEstado(GdrSolicitudConfirmacion.ESTADO_EN_CIE);
        solicitud.setFechaDerivacionCie(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return caso;
    }

    /** P4-10 — Notifica a los miembros activos del CIE; el flujo principal no se bloquea si falla. */
    private void notificarDerivacionAlCie(GdrCasoCie caso) {
        userRepository.findActiveUsernamesByRoleCode(ROL_CIE).forEach(cieUsername ->
                notificacionesService.emitForUser(
                        cieUsername,
                        NotificacionesService.CASO_CIE_DERIVADO,
                        caso.getNumeroCaso()));
    }

    private void validarSolicitudUnica(Long finalEvaluationId) {
        if (solicitudRepository.findByFinalEvaluationId(finalEvaluationId).isPresent()) {
            throw new DomainException(
                    "Ya existe una solicitud de confirmación registrada para esta evaluación. "
                    + "Consulte su estado en la sección Confirmación de calificación.");
        }
    }

    private void validarEvaluadoPropietario(String username, GdrFinalEvaluation evaluation) {
        User user = accessPolicyService.loadUserWithContext(username);
        if (accessPolicyService.isAdminSistema(user)) {
            return;
        }
        ActiveCycleContextResponse context = accessPolicyService.resolveContext(user);
        Long evaluatedId = evaluation.getAssignment().getEvaluatedPerson().getId();
        if (context.personId() == null || !Objects.equals(context.personId(), evaluatedId)) {
            throw new DomainException(
                    "Solo el evaluado titular de la evaluación puede solicitar la confirmación de su calificación.");
        }
    }

    private void validarAccesoLectura(String username, GdrFinalEvaluation evaluation) {
        User user = accessPolicyService.loadUserWithContext(username);
        if (accessPolicyService.isAdminSistema(user)
                || accessPolicyService.isOrh(user)
                || accessPolicyService.isCie(user)) {
            return;
        }
        ActiveCycleContextResponse context = accessPolicyService.resolveContext(user);
        Long evaluatedId = evaluation.getAssignment().getEvaluatedPerson().getId();
        if (context.personId() == null || !Objects.equals(context.personId(), evaluatedId)) {
            throw new DomainException(
                    "No tiene acceso a la solicitud de confirmación de esta evaluación.");
        }
    }

    private SolicitudConfirmacionResponse mapWithCaso(GdrSolicitudConfirmacion solicitud) {
        GdrCasoCie caso = casoCieRepository.findBySolicitudId(solicitud.getId()).orElse(null);
        return mapper.toSolicitudResponse(solicitud, caso);
    }

    private GdrFinalEvaluation loadEvaluation(Long finalEvaluationId) {
        return finalEvaluationRepository.findByIdInActiveCycle(finalEvaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la evaluacion final solicitada."));
    }
}
