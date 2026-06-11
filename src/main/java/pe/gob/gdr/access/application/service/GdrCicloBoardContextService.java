package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.response.CicloBoardContextResponse;
import pe.gob.gdr.access.application.dto.response.PlanningChecklistResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCieConformacion;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCieConformacionRepository;
import pe.gob.gdr.access.domain.repository.GdrCronogramaRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrIndicatorRepository;

/**
 * Computa el contexto del tablero GDR y el checklist de planificación.
 * Referencia: RPE 068-2020-SERVIR-PE, RPE 076-2021-SERVIR-PE.
 */
@Service
@Transactional(readOnly = true)
public class GdrCicloBoardContextService {

    private static final int TOTAL_ETAPAS_CRONOGRAMA = 7;
    private static final long DIAS_MINIMOS_SEGUIMIENTO = 180L;

    private static final List<String> ETAPA_ORDER = List.of(
            "BORRADOR", "EN_PLANIFICACION", "EN_SEGUIMIENTO",
            "EN_EVALUACION", "EN_CONFIRMACION",
            "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO");

    private final ActiveCycleRepository activeCycleRepository;
    private final GdrCronogramaRepository cronogramaRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final GdrGoalRepository goalRepository;
    private final GdrIndicatorRepository indicatorRepository;
    private final GdrCieConformacionRepository cieConformacionRepository;

    public GdrCicloBoardContextService(
            ActiveCycleRepository activeCycleRepository,
            GdrCronogramaRepository cronogramaRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            GdrGoalRepository goalRepository,
            GdrIndicatorRepository indicatorRepository,
            GdrCieConformacionRepository cieConformacionRepository) {
        this.activeCycleRepository = activeCycleRepository;
        this.cronogramaRepository = cronogramaRepository;
        this.assignmentRepository = assignmentRepository;
        this.goalRepository = goalRepository;
        this.indicatorRepository = indicatorRepository;
        this.cieConformacionRepository = cieConformacionRepository;
    }

    public CicloBoardContextResponse getBoardContext(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        ChecklistData data = computeChecklist(cycleId, cycle);

        boolean seguimientoMinimo = checkSeguimientoMinimo(cycle, cycleId);

        boolean canAdvance = "EN_PLANIFICACION".equals(cycle.getEstadoEtapa())
                && data.cronogramaCompleto()
                && seguimientoMinimo
                && data.participantesRegistrados()
                && data.asignacionesCompletas()
                && data.indicadoresHabilitados()
                && data.metasFormalizadas100();

        // D-02: excepción editorial en EN_SEGUIMIENTO para ORH con rol técnico especial
        boolean hasExcepcionEdicion = currentUserHasAuthority("ROLE_GDR_CICLO_EDITAR_EXCEPCIONAL");

        return new CicloBoardContextResponse(
                cycle.getId(),
                cycle.getEstadoEtapa(),
                mapEtapaLabel(cycle.getEstadoEtapa()),
                canAdvance,
                hasExcepcionEdicion,
                null,   // planificacionCompletadaEn: no persiste aún
                null,   // planificacionCompletadaPor: no persiste aún
                data.cronogramaCompleto(),
                seguimientoMinimo,
                data.participantesRegistrados(),
                data.asignacionesCompletas(),
                data.cieAplica(),
                data.cieConfigurado(),
                data.indicadoresHabilitados(),
                data.metasFormalizadas100(),
                data.notificacionesTomaConocimiento()
        );
    }

    public PlanningChecklistResponse getPlanningChecklist(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        ChecklistData data = computeChecklist(cycleId, cycle);
        boolean seguimientoMinimo = checkSeguimientoMinimo(cycle, cycleId);

        List<String> pendientes = new ArrayList<>();
        List<String> bloqueantes = new ArrayList<>();

        if (!data.cronogramaCompleto()) bloqueantes.add("Cronograma normativo incompleto (7 etapas requeridas)");
        if (!seguimientoMinimo)         bloqueantes.add("Seguimiento < 180 días (VAL-01 / RPE 068-2020 Art. 26)");
        if (!data.participantesRegistrados()) bloqueantes.add("Sin participantes GDR registrados para este ciclo");
        if (!data.asignacionesCompletas())    bloqueantes.add("Asignaciones evaluador-evaluado incompletas");
        if (!data.indicadoresHabilitados())   bloqueantes.add("Sin indicadores habilitados en el sistema");
        if (!data.metasFormalizadas100())     bloqueantes.add("Hay evaluados con metas cuyos pesos no suman 100%");
        if (data.cieAplica() && !data.cieConfigurado()) pendientes.add("CIE aún no conformado para este ciclo");
        if (!data.notificacionesTomaConocimiento())     pendientes.add("Notificaciones de toma de conocimiento pendientes");

        boolean[] requiredItems = {
            data.cronogramaCompleto(), seguimientoMinimo,
            data.participantesRegistrados(), data.asignacionesCompletas(),
            data.indicadoresHabilitados(), data.metasFormalizadas100()
        };
        int fulfilled = 0;
        for (boolean b : requiredItems) if (b) fulfilled++;
        int porcentaje = Math.round((fulfilled * 100f) / requiredItems.length);

        int conteoIntegrantesCie = 0;
        if (data.cieAplica()) {
            List<GdrCieConformacion> conformaciones =
                    cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(cycleId);
            if (!conformaciones.isEmpty()) {
                conteoIntegrantesCie = conformaciones.get(0).getIntegrantes().size();
            }
        }

        return new PlanningChecklistResponse(
                data.cronogramaCompleto(),
                seguimientoMinimo,
                data.participantesRegistrados(),
                data.asignacionesCompletas(),
                data.cieAplica(),
                data.cieConfigurado(),
                conteoIntegrantesCie,
                data.indicadoresHabilitados(),
                data.metasFormalizadas100(),
                data.notificacionesTomaConocimiento(),
                porcentaje,
                pendientes,
                bloqueantes
        );
    }

    // ── internos ──────────────────────────────────────────────────────────────

    private ChecklistData computeChecklist(Long cycleId, ActiveCycle cycle) {
        // REQ-01: cronograma con las 7 etapas normativas
        boolean cronogramaCompleto =
                cronogramaRepository.findByCycleIdOrderByFechaInicio(cycleId).size() >= TOTAL_ETAPAS_CRONOGRAMA;

        // REQ-03/04: participantes y asignaciones
        var assignments = assignmentRepository.findActiveAssignmentsByCycle(cycleId);
        boolean participantesRegistrados = !assignments.isEmpty();
        boolean asignacionesCompletas = participantesRegistrados
                && assignments.stream().allMatch(a -> assignmentRepository.hasActiveGoals(a.getId()));

        // REQ-05: indicadores habilitados a nivel sistema (RPE 076-2021 Art. 8)
        boolean indicadoresHabilitados = !indicatorRepository.findActive().isEmpty();

        // REQ-06: pesos de metas suman 100% por evaluado
        boolean metasFormalizadas100 = participantesRegistrados
                && goalRepository.findEvaluadosConPesoIncorrectoEnCiclo(cycleId).isEmpty();

        // REQ-07: CIE
        boolean cieAplica = isEtapaAfterOrEqual(cycle.getEstadoEtapa(), "EN_CONFIRMACION")
                || !cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(cycleId).isEmpty();
        boolean cieConfigurado = cieConformacionRepository.existsConformacionVigenteParaCiclo(cycleId);

        // REQ-08: notificaciones — sin query por ciclo disponible aún
        boolean notificacionesTomaConocimiento = false;

        return new ChecklistData(
                cronogramaCompleto, participantesRegistrados, asignacionesCompletas,
                indicadoresHabilitados, metasFormalizadas100, cieAplica, cieConfigurado,
                notificacionesTomaConocimiento);
    }

    private boolean checkSeguimientoMinimo(ActiveCycle cycle, Long cycleId) {
        LocalDate finSeguimiento = cycle.getFechaFinSeguimiento();
        if (finSeguimiento == null) return false;
        LocalDate inicio = cycle.getStartDate();
        if (inicio == null) {
            // Ciclos seedeados sin START_DATE: fallback al inicio de PLANIFICACION del cronograma
            inicio = cronogramaRepository.findByCycleIdOrderByFechaInicio(cycleId).stream()
                    .filter(e -> "PLANIFICACION".equals(e.getEtapa()))
                    .map(pe.gob.gdr.access.domain.model.GdrCronograma::getFechaInicio)
                    .findFirst()
                    .orElse(null);
        }
        if (inicio == null) return false;
        return ChronoUnit.DAYS.between(inicio, finSeguimiento) >= DIAS_MINIMOS_SEGUIMIENTO;
    }

    private boolean isEtapaAfterOrEqual(String estadoEtapa, String reference) {
        int idx = ETAPA_ORDER.indexOf(estadoEtapa);
        int refIdx = ETAPA_ORDER.indexOf(reference);
        return idx >= 0 && idx >= refIdx;
    }

    private ActiveCycle loadCycle(Long cycleId) {
        return activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo GDR no encontrado: " + cycleId));
    }

    private String mapEtapaLabel(String etapa) {
        return switch (etapa) {
            case "BORRADOR"                  -> "Borrador";
            case "EN_PLANIFICACION"          -> "En planificación";
            case "EN_SEGUIMIENTO"            -> "En seguimiento";
            case "EN_EVALUACION"             -> "En evaluación";
            case "EN_CONFIRMACION"           -> "En confirmación";
            case "EN_RENDIMIENTO_DISTINGUIDO" -> "Rendimiento distinguido";
            case "CERRADO"                   -> "Cerrado";
            case "ANULADO"                   -> "Anulado";
            default                          -> etapa;
        };
    }

    private boolean currentUserHasAuthority(String authority) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private record ChecklistData(
            boolean cronogramaCompleto,
            boolean participantesRegistrados,
            boolean asignacionesCompletas,
            boolean indicadoresHabilitados,
            boolean metasFormalizadas100,
            boolean cieAplica,
            boolean cieConfigurado,
            boolean notificacionesTomaConocimiento
    ) {}
}
