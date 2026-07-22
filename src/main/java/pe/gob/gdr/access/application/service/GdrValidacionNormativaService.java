package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.calendar.PeruBusinessDayCalendar;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;

/**
 * Validaciones normativas del ciclo GDR.
 * Referencia: RPE 068-2020-SERVIR-PE.
 */
@Service
public class GdrValidacionNormativaService {

    // VAL-01 — Seguimiento mínimo de 6 meses (Art. 26)
    private static final long DIAS_MINIMOS_SEGUIMIENTO = 180L;
    // VAL-03 — Evaluación debe cerrarse antes del 31/01 del año siguiente
    private static final int MES_LIMITE_EVALUACION = 1;
    private static final int DIA_LIMITE_EVALUACION = 31;
    // VAL-05 — Informe de cierre hasta 31/05 del año siguiente
    private static final int MES_LIMITE_INFORME = 5;
    private static final int DIA_LIMITE_INFORME = 31;

    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final GdrCasoCieRepository casoCieRepository;

    public GdrValidacionNormativaService(
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            GdrCasoCieRepository casoCieRepository) {
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.assignmentRepository = assignmentRepository;
        this.casoCieRepository = casoCieRepository;
    }

    /**
     * Valida que haya transcurrido el mínimo de 6 meses de seguimiento.
     * Se compara desde START_DATE del ciclo hasta la fecha de fin de seguimiento.
     * Lanza DomainException si no se cumple (VAL-01).
     */
    public void validarSeguimientoMinimo6Meses(ActiveCycle cycle) {
        LocalDate inicio = cycle.getStartDate();
        LocalDate finSeguimiento = cycle.getFechaFinSeguimiento();
        if (inicio == null || finSeguimiento == null) {
            throw new DomainException(
                    "El ciclo no tiene fechas de inicio o fin de seguimiento definidas. "
                    + "Configure el cronograma antes de avanzar de etapa.");
        }
        long dias = ChronoUnit.DAYS.between(inicio, finSeguimiento);
        if (dias < DIAS_MINIMOS_SEGUIMIENTO) {
            throw new DomainException(String.format(
                    "El período de seguimiento (%d días) es menor al mínimo normativo de %d días (6 meses). "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 26.",
                    dias, DIAS_MINIMOS_SEGUIMIENTO));
        }
    }

    /**
     * Valida que la fecha de cierre de evaluación no supere el 31 de enero
     * del año siguiente al inicio del ciclo (VAL-03).
     */
    public void validarFechaEvaluacionLimite(ActiveCycle cycle) {
        LocalDate inicio = cycle.getStartDate();
        if (inicio == null) {
            return;
        }
        LocalDate limiteEvaluacion = LocalDate.of(
                inicio.getYear() + 1, MES_LIMITE_EVALUACION, DIA_LIMITE_EVALUACION);
        if (LocalDate.now().isAfter(limiteEvaluacion)) {
            throw new DomainException(String.format(
                    "La fecha límite para cerrar evaluaciones (%s) ya venció. "
                    + "No se puede avanzar a EN_EVALUACION fuera de plazo. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 5 Etapa 3.",
                    limiteEvaluacion));
        }
    }

    /**
     * Calcula y retorna la fecha límite de informe de cierre (31/mayo año siguiente).
     */
    public LocalDate calcularFechaLimiteInforme(ActiveCycle cycle) {
        if (cycle.getStartDate() == null) {
            return null;
        }
        return LocalDate.of(
                cycle.getStartDate().getYear() + 1, MES_LIMITE_INFORME, DIA_LIMITE_INFORME);
    }

    /**
     * Calcula la fecha fin normativa de seguimiento (31/12 del año del ciclo).
     */
    public LocalDate calcularFechaFinNormativaSeguimiento(ActiveCycle cycle) {
        if (cycle.getStartDate() == null) {
            return null;
        }
        return LocalDate.of(cycle.getStartDate().getYear(), 12, 31);
    }

    /**
     * Calcula la fecha fin normativa de evaluación (31/01 año siguiente).
     */
    public LocalDate calcularFechaFinNormativaEvaluacion(ActiveCycle cycle) {
        if (cycle.getStartDate() == null) {
            return null;
        }
        return LocalDate.of(cycle.getStartDate().getYear() + 1, MES_LIMITE_EVALUACION, DIA_LIMITE_EVALUACION);
    }

    // ── VAL-04 — Solicitud de confirmación: 5 días hábiles (Art. 41) ──────

    /**
     * Valida que la fecha de reunión de retroalimentación final no sea futura.
     */
    public void validarFechaReunionRetroFinal(LocalDate fechaReunion) {
        if (fechaReunion == null) {
            throw new DomainException("La fecha de reunión de retroalimentación final es obligatoria.");
        }
        if (fechaReunion.isAfter(LocalDate.now())) {
            throw new DomainException(
                    "La fecha de reunión de retroalimentación final no puede ser futura.");
        }
    }

    /**
     * Calcula el plazo límite de solicitud de confirmación de calificación:
     * 5 días hábiles contados desde el día hábil siguiente a la reunión de
     * retroalimentación final (RPE 068-2020 Art. 41). Excluye sábados,
     * domingos y feriados (GDR_PUBLIC_HOLIDAY).
     */
    public LocalDate calcularPlazoSolicitudConfirmacion(LocalDate fechaReunionRetroFinal, Set<LocalDate> feriados) {
        return PeruBusinessDayCalendar.fifthBusinessDayAfterGradeEnd(fechaReunionRetroFinal, feriados);
    }

    /**
     * Cuenta los días hábiles restantes (incluyendo hoy si es hábil) hasta el
     * plazo límite. Retorna 0 si el plazo ya venció.
     */
    public int contarDiasHabilesRestantes(LocalDate hoy, LocalDate plazoLimite, Set<LocalDate> feriados) {
        return PeruBusinessDayCalendar.countBusinessDaysInclusive(hoy, plazoLimite, feriados);
    }

    /**
     * VAL-04 (bloqueo) — Valida que la solicitud de confirmación se presente
     * dentro del plazo de 5 días hábiles (RPE 068-2020 Art. 41).
     */
    public void validarSolicitudDentroDePlazo(LocalDate hoy, LocalDate plazoLimite) {
        if (plazoLimite == null) {
            throw new DomainException(
                    "Aún no se registra la reunión de retroalimentación final, por lo que el plazo "
                    + "para solicitar confirmación no está activo. Coordine con su evaluador/a.");
        }
        if (hoy.isAfter(plazoLimite)) {
            throw new DomainException(String.format(
                    "El plazo para solicitar la confirmación de calificación venció el %s "
                    + "(5 días hábiles desde la reunión de retroalimentación final). "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 41.",
                    plazoLimite));
        }
    }

    /**
     * Convocatoria CIE — Calcula la fecha límite de convocatoria del CIE:
     * 3 días hábiles desde la recepción del caso (RPE 068-2020 Art. 42).
     * Es una alerta de gestión, no un bloqueo.
     */
    public LocalDate calcularPlazoConvocatoriaCie(LocalDate fechaRecepcion, Set<LocalDate> feriados) {
        return PeruBusinessDayCalendar.nthBusinessDayAfter(
                fechaRecepcion, PeruBusinessDayCalendar.CIE_CONVOCATORIA_BUSINESS_DAYS, feriados);
    }

    /**
     * VAL-13A — Retorna el número de evaluaciones finales activas en el ciclo
     * sin retroalimentación final registrada (evaluado no notificado formalmente).
     * Referencia: RPE 068-2020-SERVIR-PE Art. 33-39.
     */
    public int contarEvaluacionesSinNotificar(Long cycleId) {
        return finalEvaluationRepository.countSinNotificarEnCiclo(cycleId);
    }

    /**
     * VAL-13A — Retorna los nombres de evaluados sin retroalimentación final registrada.
     */
    public List<String> findNombresSinNotificarEnCiclo(Long cycleId) {
        return finalEvaluationRepository.findNombresSinNotificarEnCiclo(cycleId);
    }

    /**
     * VAL-13B (bloqueo) — Impide cerrar el ciclo si existen evaluaciones finales
     * sin retroalimentación registrada. La calificación debe ser comunicada al
     * evaluado antes del cierre.
     * Referencia: RPE 068-2020-SERVIR-PE Art. 33-39.
     */
    public void validarCierreConEvaluacionesSinNotificar(Long cycleId) {
        List<String> sinNotificar = finalEvaluationRepository.findNombresSinNotificarEnCiclo(cycleId);
        if (!sinNotificar.isEmpty()) {
            String lista = String.join(", ", sinNotificar);
            throw new DomainException(String.format(
                    "No se puede cerrar el ciclo: %d evaluado(s) no han recibido retroalimentación "
                    + "final de su calificación: %s. "
                    + "Registre la fecha de reunión de retroalimentación final antes de cerrar. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 33-39.",
                    sinNotificar.size(), lista));
        }
    }

    /**
     * VAL-08 (bloqueo) — No se puede asignar Rendimiento distinguido mientras el
     * candidato tenga una confirmación de calificación pendiente ante el CIE.
     * Referencia: RPE 068-2020-SERVIR-PE Art. 50.
     */
    public void validarSinConfirmacionPendienteParaDistinguido(String evaluatedDisplayName, String estadoConfirmacion) {
        if (GdrResult.ESTADO_CONF_PENDIENTE.equals(estadoConfirmacion)) {
            throw new DomainException(String.format(
                    "No se puede asignar Rendimiento distinguido a %s porque tiene una solicitud de "
                    + "confirmación de calificación pendiente ante el Comité Institucional de Evaluación (CIE). "
                    + "Espere la resolución del CIE o verifique el estado en Confirmación de calificación. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 50.",
                    evaluatedDisplayName));
        }
    }

    /**
     * (bloqueo) — Impide avanzar a EN_CONFIRMACION si hay asignaciones activas del ciclo
     * sin evaluación final registrada. Todos los evaluados deben tener su evaluación final
     * antes de que el ciclo entre en fase de confirmación de calificación.
     * Referencia: RPE 068-2020-SERVIR-PE Art. 5 Etapa 3.
     */
    public void validarTodasEvaluacionesFinalesRegistradas(Long cycleId) {
        List<String> sinEvaluacion = assignmentRepository.findNombresSinEvaluacionFinalEnCiclo(cycleId);
        if (!sinEvaluacion.isEmpty()) {
            String lista = String.join(", ", sinEvaluacion);
            throw new DomainException(String.format(
                    "No se puede avanzar a Confirmación de calificación: %d evaluado(s) no tienen "
                    + "evaluación final registrada en este ciclo: %s. "
                    + "Complete todas las evaluaciones finales antes de avanzar. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 5 Etapa 3.",
                    sinEvaluacion.size(), lista));
        }
    }

    /**
     * (bloqueo) — Impide avanzar a EN_RENDIMIENTO_DISTINGUIDO si existen casos CIE
     * pendientes de resolución (estado RECIBIDO) en el ciclo.
     * Todos los casos deben estar resueltos antes de iniciar la fase de distinguidos.
     * Referencia: RPE 068-2020-SERVIR-PE Art. 42-48.
     */
    public void validarCasosCieTodosResueltos(Long cycleId) {
        long pendientes = casoCieRepository.countPendientesByCycleId(cycleId);
        if (pendientes > 0) {
            throw new DomainException(String.format(
                    "No se puede avanzar a Rendimiento distinguido: %d caso(s) del Comité "
                    + "Institucional de Evaluación (CIE) están pendientes de resolución. "
                    + "Resuelva todos los casos CIE antes de avanzar. "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 42-48.",
                    pendientes));
        }
    }
}
