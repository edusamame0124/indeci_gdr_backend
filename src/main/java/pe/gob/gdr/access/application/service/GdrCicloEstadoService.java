package pe.gob.gdr.access.application.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CreateCycleRequest;
import pe.gob.gdr.access.application.dto.response.CycleAccessResponse;
import pe.gob.gdr.access.application.dto.response.CycleOptionResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;

/**
 * Máquina de estados del ciclo GDR.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 5.
 *
 * Estados:
 *   BORRADOR → EN_PLANIFICACION → EN_SEGUIMIENTO → EN_EVALUACION
 *   → EN_CONFIRMACION → EN_RENDIMIENTO_DISTINGUIDO → CERRADO
 *   Cualquier estado → ANULADO (solo ADMIN_SISTEMA)
 */
@Service
public class GdrCicloEstadoService {

    public static final String BORRADOR                    = "BORRADOR";
    public static final String EN_PLANIFICACION            = "EN_PLANIFICACION";
    public static final String EN_SEGUIMIENTO              = "EN_SEGUIMIENTO";
    public static final String EN_EVALUACION               = "EN_EVALUACION";
    public static final String EN_CONFIRMACION             = "EN_CONFIRMACION";
    public static final String EN_RENDIMIENTO_DISTINGUIDO  = "EN_RENDIMIENTO_DISTINGUIDO";
    public static final String CERRADO                     = "CERRADO";
    public static final String ANULADO                     = "ANULADO";

    private static final Map<String, String> TRANSICION_SIGUIENTE = Map.of(
            BORRADOR,                   EN_PLANIFICACION,
            EN_PLANIFICACION,           EN_SEGUIMIENTO,
            EN_SEGUIMIENTO,             EN_EVALUACION,
            EN_EVALUACION,              EN_CONFIRMACION,
            EN_CONFIRMACION,            EN_RENDIMIENTO_DISTINGUIDO,
            EN_RENDIMIENTO_DISTINGUIDO, CERRADO
    );

    private static final Map<String, String> ETAPA_LABEL = Map.of(
            BORRADOR,                  "Borrador",
            EN_PLANIFICACION,          "En planificación",
            EN_SEGUIMIENTO,            "En seguimiento",
            EN_EVALUACION,             "En evaluación",
            EN_CONFIRMACION,           "En confirmación de calificación",
            EN_RENDIMIENTO_DISTINGUIDO,"En rendimiento distinguido",
            CERRADO,                   "Cerrado",
            ANULADO,                   "Anulado"
    );

    private final ActiveCycleRepository activeCycleRepository;
    private final GdrValidacionNormativaService validacion;
    private final AuditTrailService auditTrailService;

    public GdrCicloEstadoService(
            ActiveCycleRepository activeCycleRepository,
            GdrValidacionNormativaService validacion,
            AuditTrailService auditTrailService) {
        this.activeCycleRepository = activeCycleRepository;
        this.validacion = validacion;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Crea un nuevo ciclo GDR en estado BORRADOR.
     * Solo permite la creación si no existe un ciclo en curso (estadoEtapa != CERRADO/ANULADO).
     * Restricción P1: un ciclo activo a la vez (multi-ciclo real queda para P2).
     */
    @Transactional
    public CycleOptionResponse createCycle(CreateCycleRequest request) {
        activeCycleRepository.findActiveCycle().ifPresent(existing -> {
            String etapa = existing.getEstadoEtapa();
            if (!CERRADO.equals(etapa) && !ANULADO.equals(etapa)) {
                throw new DomainException(
                        "No se puede crear un nuevo ciclo mientras existe uno en curso. "
                        + "Estado actual: " + labelFor(etapa)
                        + ". Debe cerrarlo o anularlo antes de crear uno nuevo.");
            }
        });

        if (!request.startDate().isBefore(request.endDate())) {
            throw new DomainException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }

        ActiveCycle cycle = ActiveCycle.builder()
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .estadoEtapa(BORRADOR)
                .status("ACTIVE")
                .build();

        ActiveCycle saved = activeCycleRepository.save(cycle);
        return new CycleOptionResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getStatus(),
                saved.getStartDate(),
                saved.getEndDate(),
                true
        );
    }

    /**
     * Valida que un ciclo existe y retorna su estado junto con el flag isActiveCycle.
     * Usado por requireCycleGuard en el frontend para determinar si el ciclo
     * navegado es el activo del backend (y así controlar módulos backend-singleton).
     */
    @Transactional(readOnly = true)
    public CycleAccessResponse validateCycleAccess(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        Long activeCycleId = activeCycleRepository.findActiveCycle()
                .map(ActiveCycle::getId)
                .orElse(null);
        return new CycleAccessResponse(
                cycle.getId(),
                cycle.getCode(),
                cycle.getName(),
                cycle.getStatus(),
                cycle.getEstadoEtapa(),
                labelFor(cycle.getEstadoEtapa()),
                Objects.equals(cycle.getId(), activeCycleId),
                true
        );
    }

    /** Retorna la etiqueta legible de un estado. */
    public String labelFor(String estado) {
        return ETAPA_LABEL.getOrDefault(estado, estado);
    }

    /** Retorna la lista de transiciones disponibles desde el estado actual. */
    public List<String> transicionesDisponibles(String estadoActual) {
        if (TRANSICION_SIGUIENTE.containsKey(estadoActual)) {
            return List.of(TRANSICION_SIGUIENTE.get(estadoActual));
        }
        return List.of();
    }

    /**
     * Avanza el ciclo al siguiente estado normativo.
     * Aplica validaciones normativas dependientes del estado destino.
     */
    @Transactional
    public ActiveCycle avanzarEtapa(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        String estadoActual = cycle.getEstadoEtapa();

        if (!TRANSICION_SIGUIENTE.containsKey(estadoActual)) {
            throw new DomainException(String.format(
                    "El ciclo no puede avanzar desde el estado '%s'. "
                    + "Estados finales: CERRADO, ANULADO.", estadoActual));
        }

        String estadoDestino = TRANSICION_SIGUIENTE.get(estadoActual);
        validarTransicion(cycle, estadoDestino);

        cycle.setEstadoEtapa(estadoDestino);
        actualizarFechasEtapa(cycle, estadoDestino);
        ActiveCycle saved = activeCycleRepository.save(cycle);

        String principal = resolveCurrentPrincipal();
        auditTrailService.recordEvent(
                "GDR_CICLO_AVANZAR_ETAPA",
                principal,
                String.format("Ciclo %s (%s) avanzó de %s a %s.",
                        cycle.getCode(), cycle.getId(), estadoActual, estadoDestino),
                null);
        return saved;
    }

    /**
     * Anula el ciclo (solo disponible para ADMIN_SISTEMA).
     * No aplica validaciones normativas — decisión administrativa.
     */
    @Transactional
    public ActiveCycle anular(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        if (CERRADO.equals(cycle.getEstadoEtapa()) || ANULADO.equals(cycle.getEstadoEtapa())) {
            throw new DomainException("No se puede anular un ciclo ya cerrado o anulado.");
        }
        cycle.setEstadoEtapa(ANULADO);
        return activeCycleRepository.save(cycle);
    }

    // ── private ────────────────────────────────────────────────────────────

    private void validarTransicion(ActiveCycle cycle, String estadoDestino) {
        switch (estadoDestino) {
            case EN_SEGUIMIENTO -> {
                // VAL-01: seguimiento mínimo 6 meses configurado en cronograma
                validacion.validarSeguimientoMinimo6Meses(cycle);
                // VAL-07: todos los evaluados deben tener metas con pesos = 100%
                validacion.validarPesosTotalMetasPorCiclo(cycle.getId());
            }
            case EN_EVALUACION -> validacion.validarFechaEvaluacionLimite(cycle);
            // Todos los evaluados deben tener evaluación final antes de confirmar
            case EN_CONFIRMACION -> validacion.validarTodasEvaluacionesFinalesRegistradas(cycle.getId());
            // Todos los casos CIE deben estar resueltos antes de pasar a distinguidos
            case EN_RENDIMIENTO_DISTINGUIDO -> validacion.validarCasosCieTodosResueltos(cycle.getId());
            // VAL-13B: bloqueo si hay evaluaciones sin retroalimentación final registrada
            case CERRADO -> validacion.validarCierreConEvaluacionesSinNotificar(cycle.getId());
            default -> { /* sin validación adicional */ }
        }
    }

    private void actualizarFechasEtapa(ActiveCycle cycle, String estadoDestino) {
        switch (estadoDestino) {
            case EN_EVALUACION -> {
                if (cycle.getFechaLimiteInforme() == null) {
                    cycle.setFechaLimiteInforme(validacion.calcularFechaLimiteInforme(cycle));
                }
            }
            default -> { /* no action */ }
        }
    }

    private ActiveCycle loadCycle(Long cycleId) {
        return activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado: " + cycleId));
    }

    private String resolveCurrentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }
}
