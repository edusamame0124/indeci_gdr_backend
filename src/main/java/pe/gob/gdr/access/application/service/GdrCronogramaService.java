package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CronogramaEtapaRequest;
import pe.gob.gdr.access.application.dto.response.CicloConCronogramaResponse;
import pe.gob.gdr.access.application.dto.response.CronogramaEtapaResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCronograma;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCronogramaRepository;

@Service
public class GdrCronogramaService {

    private static final Set<String> ETAPAS_VALIDAS = Set.of(
            "PLANIFICACION", "SEGUIMIENTO", "EVALUACION",
            "RETROALIMENTACION", "CONFIRMACION", "DISTINGUIDO", "CIERRE");

    private final GdrCronogramaRepository cronogramaRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final GdrValidacionNormativaService validacion;
    private final GdrCicloEstadoService cicloEstadoService;

    public GdrCronogramaService(
            GdrCronogramaRepository cronogramaRepository,
            ActiveCycleRepository activeCycleRepository,
            GdrValidacionNormativaService validacion,
            GdrCicloEstadoService cicloEstadoService) {
        this.cronogramaRepository = cronogramaRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.validacion = validacion;
        this.cicloEstadoService = cicloEstadoService;
    }

    @Transactional(readOnly = true)
    public CicloConCronogramaResponse getCicloCronograma(Long cycleId) {
        ActiveCycle cycle = loadCycle(cycleId);
        List<GdrCronograma> etapas = cronogramaRepository.findByCycleIdOrderByFechaInicio(cycleId);
        return toCicloConCronograma(cycle, etapas);
    }

    @Transactional(readOnly = true)
    public CicloConCronogramaResponse getCicloCronogramaActivo() {
        ActiveCycle cycle = activeCycleRepository.findActiveCycle()
                .orElseThrow(() -> new DomainException("No hay ciclo GDR activo."));
        List<GdrCronograma> etapas = cronogramaRepository.findByCycleIdOrderByFechaInicio(cycle.getId());
        return toCicloConCronograma(cycle, etapas);
    }

    /**
     * Crea o actualiza una entrada del cronograma para una etapa dada.
     * Solo ORH puede invocar esto. Actualiza fechas de control en GDR_CYCLE si aplica.
     */
    @Transactional
    public CronogramaEtapaResponse upsertEtapa(Long cycleId, String etapa, CronogramaEtapaRequest request) {
        final String etapaUpper = etapa.toUpperCase();
        if (!ETAPAS_VALIDAS.contains(etapaUpper)) {
            throw new DomainException("Etapa no válida: " + etapaUpper
                    + ". Valores permitidos: " + ETAPAS_VALIDAS);
        }
        if (request.fechaInicio() == null || request.fechaFin() == null) {
            throw new DomainException("fechaInicio y fechaFin son obligatorios.");
        }
        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new DomainException("fechaFin no puede ser anterior a fechaInicio.");
        }

        ActiveCycle cycle = loadCycle(cycleId);
        GdrCronograma cronograma = cronogramaRepository
                .findByCycleIdAndEtapa(cycleId, etapaUpper)
                .orElseGet(() -> GdrCronograma.builder().cycle(cycle).etapa(etapaUpper).build());

        cronograma.setFechaInicio(request.fechaInicio());
        cronograma.setFechaFin(request.fechaFin());
        cronograma.setFechaFinNormativa(calcularFechaFinNormativa(etapaUpper, cycle));

        actualizarFechaControlCiclo(cycle, etapaUpper, request.fechaFin());
        activeCycleRepository.save(cycle);

        return toResponse(cronogramaRepository.save(cronograma));
    }

    // ── private helpers ────────────────────────────────────────────────────

    private void actualizarFechaControlCiclo(ActiveCycle cycle, String etapa, LocalDate fechaFin) {
        switch (etapa) {
            case "SEGUIMIENTO" -> cycle.setFechaFinSeguimiento(fechaFin);
            case "EVALUACION"  -> cycle.setFechaFinEvaluacion(fechaFin);
            case "CIERRE"      -> {
                if (cycle.getFechaLimiteInforme() == null) {
                    cycle.setFechaLimiteInforme(validacion.calcularFechaLimiteInforme(cycle));
                }
            }
            default -> { /* no action */ }
        }
    }

    private LocalDate calcularFechaFinNormativa(String etapa, ActiveCycle cycle) {
        return switch (etapa) {
            case "SEGUIMIENTO" -> validacion.calcularFechaFinNormativaSeguimiento(cycle);
            case "EVALUACION"  -> validacion.calcularFechaFinNormativaEvaluacion(cycle);
            case "CIERRE"      -> validacion.calcularFechaLimiteInforme(cycle);
            default -> null;
        };
    }

    private CicloConCronogramaResponse toCicloConCronograma(
            ActiveCycle cycle, List<GdrCronograma> etapas) {
        List<CronogramaEtapaResponse> cronogramaResp = etapas.stream()
                .map(this::toResponse)
                .toList();
        List<String> transiciones = cicloEstadoService.transicionesDisponibles(cycle.getEstadoEtapa());
        return new CicloConCronogramaResponse(
                cycle.getId(),
                cycle.getCode(),
                cycle.getName(),
                cycle.getStatus(),
                cycle.getEstadoEtapa(),
                cicloEstadoService.labelFor(cycle.getEstadoEtapa()),
                cycle.getStartDate(),
                cycle.getEndDate(),
                cycle.getFechaFinSeguimiento(),
                cycle.getFechaFinEvaluacion(),
                cycle.getFechaLimiteInforme(),
                "ACTIVE".equalsIgnoreCase(cycle.getStatus()),
                cronogramaResp,
                transiciones
        );
    }

    private CronogramaEtapaResponse toResponse(GdrCronograma c) {
        LocalDate hoy = LocalDate.now();
        boolean vencida = c.getFechaFinNormativa() != null && hoy.isAfter(c.getFechaFinNormativa());
        long diasRestantes = c.getFechaFin() != null
                ? ChronoUnit.DAYS.between(hoy, c.getFechaFin())
                : 0L;
        return new CronogramaEtapaResponse(
                c.getId(),
                c.getEtapa(),
                etapaLabel(c.getEtapa()),
                c.getFechaInicio(),
                c.getFechaFin(),
                c.getFechaFinNormativa(),
                c.getEstado(),
                vencida,
                diasRestantes
        );
    }

    private String etapaLabel(String etapa) {
        return switch (etapa) {
            case "PLANIFICACION"    -> "Planificación";
            case "SEGUIMIENTO"      -> "Seguimiento";
            case "EVALUACION"       -> "Evaluación";
            case "RETROALIMENTACION"-> "Retroalimentación";
            case "CONFIRMACION"     -> "Confirmación de calificación";
            case "DISTINGUIDO"      -> "Rendimiento distinguido";
            case "CIERRE"           -> "Cierre del ciclo";
            default -> etapa;
        };
    }

    private ActiveCycle loadCycle(Long cycleId) {
        return activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado: " + cycleId));
    }
}
