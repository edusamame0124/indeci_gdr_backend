package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.request.RegistrarRetroFinalRequest;
import pe.gob.gdr.access.application.dto.response.DetalleEvaluacionFinalResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;
import pe.gob.gdr.access.domain.repository.GdrScoreDetailRepository;

/**
 * P3 — Retroalimentación final formal y plazo de solicitud de confirmación
 * (VAL-04, RPE 068-2020 Art. 41).
 */
@ExtendWith(MockitoExtension.class)
class GdrFinalEvaluationServiceTest {

    @Mock GdrEvaluationAssignmentRepository assignmentRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrEvidenceRepository evidenceRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;
    @Mock GdrScoreDetailRepository scoreDetailRepository;
    @Mock GdrResultService resultService;
    @Mock GdrAccessPolicyService accessPolicyService;
    @Mock GdrPublicHolidayRepository publicHolidayRepository;
    @Mock pe.gob.gdr.access.domain.repository.GdrCasoCieRepository casoCieRepository;

    private GdrFinalEvaluationService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrFinalEvaluationService(
                assignmentRepository, goalRepository, evidenceRepository,
                finalEvaluationRepository, scoreDetailRepository,
                resultService, accessPolicyService,
                publicHolidayRepository, new GdrValidacionNormativaService(
                        finalEvaluationRepository,
                        assignmentRepository, casoCieRepository));
    }

    // ── registrarRetroalimentacionFinal — caso feliz ────────────────────

    @Test
    void registrarRetroFinal_casoFeliz_calculaPlazo5DiasHabiles() {
        GdrFinalEvaluation evaluation = evaluacionActiva(50L);
        when(finalEvaluationRepository.findByIdAndCycle(50L, 1L)).thenReturn(Optional.of(evaluation));
        when(finalEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(publicHolidayRepository.findHolidayDatesBetween(any(), any())).thenReturn(Set.of());
        when(scoreDetailRepository.findByFinalEvaluationId(anyLong())).thenReturn(List.of());

        // Viernes 09/01/2026 → 5 días hábiles: 12, 13, 14, 15, 16 → viernes 16/01/2026
        LocalDate fechaReunion = LocalDate.of(2026, 1, 9);
        DetalleEvaluacionFinalResponse resp = sut.registrarRetroalimentacionFinal(
                50L, new RegistrarRetroFinalRequest(fechaReunion), 1L);

        assertThat(resp.fechaReunionRetroFinal()).isEqualTo(fechaReunion);
        assertThat(resp.plazoSolicitudConfirmacion()).isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(evaluation.getFechaReunionRetroFinal()).isEqualTo(fechaReunion);
        assertThat(evaluation.getPlazoSolicitudConfirmacion()).isEqualTo(LocalDate.of(2026, 1, 16));
    }

    // ── error normativo: fecha futura ───────────────────────────────────

    @Test
    void registrarRetroFinal_fechaFutura_lanzaError() {
        when(finalEvaluationRepository.findByIdAndCycle(50L, 1L))
                .thenReturn(Optional.of(evaluacionActiva(50L)));

        RegistrarRetroFinalRequest req = new RegistrarRetroFinalRequest(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> sut.registrarRetroalimentacionFinal(50L, req, 1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("futura");
    }

    // ── error: evaluación inexistente ───────────────────────────────────

    @Test
    void registrarRetroFinal_evaluacionInexistente_lanzaResourceNotFound() {
        when(finalEvaluationRepository.findByIdAndCycle(99L, 1L)).thenReturn(Optional.empty());

        RegistrarRetroFinalRequest req = new RegistrarRetroFinalRequest(LocalDate.of(2026, 1, 9));

        assertThatThrownBy(() -> sut.registrarRetroalimentacionFinal(99L, req, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── caso borde: feriado dentro del plazo lo extiende ────────────────

    @Test
    void registrarRetroFinal_feriadoDentroDelPlazo_extiendePlazo() {
        GdrFinalEvaluation evaluation = evaluacionActiva(50L);
        when(finalEvaluationRepository.findByIdAndCycle(50L, 1L)).thenReturn(Optional.of(evaluation));
        when(finalEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Lunes 12/01/2026 feriado → hábiles: 13, 14, 15, 16, 19 → lunes 19/01/2026
        when(publicHolidayRepository.findHolidayDatesBetween(any(), any()))
                .thenReturn(Set.of(LocalDate.of(2026, 1, 12)));
        when(scoreDetailRepository.findByFinalEvaluationId(anyLong())).thenReturn(List.of());

        DetalleEvaluacionFinalResponse resp = sut.registrarRetroalimentacionFinal(
                50L, new RegistrarRetroFinalRequest(LocalDate.of(2026, 1, 9)), 1L);

        assertThat(resp.plazoSolicitudConfirmacion()).isEqualTo(LocalDate.of(2026, 1, 19));
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private GdrFinalEvaluation evaluacionActiva(Long id) {
        HrPerson evaluado = HrPerson.builder().id(2L).displayName("EVALUADO PRUEBA").build();
        HrPerson evaluador = HrPerson.builder().id(3L).displayName("EVALUADOR PRUEBA").build();
        ActiveCycle cycle = ActiveCycle.builder()
                .id(1L).code("C-2026").name("Ciclo 2026")
                .startDate(LocalDate.of(2026, 1, 1)).status("ACTIVE")
                .build();
        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .id(1L)
                .cycle(cycle)
                .evaluatedPerson(evaluado)
                .evaluatorPerson(evaluador)
                .build();
        return GdrFinalEvaluation.builder()
                .id(id)
                .assignment(assignment)
                .status("ACTIVE")
                .build();
    }
}
