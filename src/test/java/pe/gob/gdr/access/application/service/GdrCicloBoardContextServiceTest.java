package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.response.CicloBoardContextResponse;
import pe.gob.gdr.access.application.dto.response.PlanningChecklistResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCieConformacionRepository;
import pe.gob.gdr.access.domain.repository.GdrCronogramaRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrIndicatorRepository;

@ExtendWith(MockitoExtension.class)
class GdrCicloBoardContextServiceTest {

    @Mock ActiveCycleRepository activeCycleRepository;
    @Mock GdrCronogramaRepository cronogramaRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrIndicatorRepository indicatorRepository;
    @Mock GdrCieConformacionRepository cieConformacionRepository;

    private GdrCicloBoardContextService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrCicloBoardContextService(
                activeCycleRepository,
                cronogramaRepository,
                assignmentRepository,
                goalRepository,
                indicatorRepository,
                cieConformacionRepository);
    }

    // ── getBoardContext — casos principales ───────────────────────────────

    @Test
    void getBoardContext_retornaEstadoCorrectamente() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        stubChecklistMocks(cycle.getId(), true);
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        CicloBoardContextResponse response = sut.getBoardContext(1L);

        assertThat(response.estadoEtapa()).isEqualTo("EN_PLANIFICACION");
        assertThat(response.estadoEtapaLabel()).isEqualTo("En planificación");
        assertThat(response.cycleId()).isEqualTo(1L);
    }

    @Test
    void getBoardContext_canAdvance_verdadero_cuandoTodosRequisitosOk() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        stubChecklistMocks(cycle.getId(), true);
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        CicloBoardContextResponse response = sut.getBoardContext(1L);

        assertThat(response.canAdvanceToSeguimiento()).isTrue();
    }

    @Test
    void getBoardContext_canAdvance_falso_sinCronograma() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        // cronograma vacío → cronogramaCompleto = false
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(cronogramaRepository.findByCycleIdOrderByFechaInicio(1L)).thenReturn(List.of());
        when(assignmentRepository.findActiveAssignmentsByCycle(1L)).thenReturn(List.of());
        when(indicatorRepository.findActive()).thenReturn(List.of());
        when(cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(1L)).thenReturn(List.of());

        CicloBoardContextResponse response = sut.getBoardContext(1L);

        assertThat(response.canAdvanceToSeguimiento()).isFalse();
        assertThat(response.cronogramaCompleto()).isFalse();
    }

    @Test
    void getBoardContext_cicloInexistente_lanzaResourceNotFound() {
        when(activeCycleRepository.findByIdForAdministration(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getBoardContext(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getPlanningChecklist — casos principales ──────────────────────────

    @Test
    void getPlanningChecklist_todoOk_porcentaje100() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        cycle.setFechaFinSeguimiento(LocalDate.of(2025, 8, 1)); // >180 días desde 2025-01-01
        stubChecklistMocks(cycle.getId(), true);
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        PlanningChecklistResponse checklist = sut.getPlanningChecklist(1L);

        assertThat(checklist.cronogramaCompleto()).isTrue();
        assertThat(checklist.participantesRegistrados()).isTrue();
        assertThat(checklist.asignacionesCompletas()).isTrue();
        assertThat(checklist.indicadoresHabilitados()).isTrue();
        assertThat(checklist.metasFormalizadas100()).isTrue();
        assertThat(checklist.seguimientoMinimoSeisMeses()).isTrue();
        assertThat(checklist.porcentajeAvance()).isEqualTo(100);
        assertThat(checklist.bloqueantes()).isEmpty();
    }

    @Test
    void getPlanningChecklist_sinFechaSeguimiento_bloqueantePorVAL01() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        // fechaFinSeguimiento null → seguimientoMinimo = false
        stubChecklistMocks(cycle.getId(), true);
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        PlanningChecklistResponse checklist = sut.getPlanningChecklist(1L);

        assertThat(checklist.seguimientoMinimoSeisMeses()).isFalse();
        assertThat(checklist.bloqueantes()).anyMatch(b -> b.contains("VAL-01"));
    }

    @Test
    void getPlanningChecklist_sinParticipantes_bloqueantePorParticipantes() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(cronogramaRepository.findByCycleIdOrderByFechaInicio(1L)).thenReturn(fakeEtapas(7));
        when(assignmentRepository.findActiveAssignmentsByCycle(1L)).thenReturn(List.of());
        when(indicatorRepository.findActive()).thenReturn(List.of());
        when(cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(1L)).thenReturn(List.of());

        PlanningChecklistResponse checklist = sut.getPlanningChecklist(1L);

        assertThat(checklist.participantesRegistrados()).isFalse();
        assertThat(checklist.bloqueantes()).anyMatch(b -> b.toLowerCase().contains("participantes"));
    }

    @Test
    void getPlanningChecklist_metasConPesoIncorrecto_bloqueante() {
        ActiveCycle cycle = cycleEN_PLANIFICACION();
        GdrEvaluationAssignment assignment = fakeAssignment(10L);
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(cronogramaRepository.findByCycleIdOrderByFechaInicio(1L)).thenReturn(fakeEtapas(7));
        when(assignmentRepository.findActiveAssignmentsByCycle(1L)).thenReturn(List.of(assignment));
        when(assignmentRepository.hasActiveGoals(10L)).thenReturn(true);
        when(indicatorRepository.findActive()).thenReturn(List.of());
        when(goalRepository.findEvaluadosConPesoIncorrectoEnCiclo(1L)).thenReturn(List.of("Juan Perez"));
        when(cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(1L)).thenReturn(List.of());

        PlanningChecklistResponse checklist = sut.getPlanningChecklist(1L);

        assertThat(checklist.metasFormalizadas100()).isFalse();
        assertThat(checklist.bloqueantes()).anyMatch(b -> b.contains("100%"));
    }

    // ── GdrCicloModuloEtapaPolicy — tests unitarios ───────────────────────

    @Test
    void policy_cronogramaAccesibleEnBorrador() {
        GdrCicloModuloEtapaPolicy policy = new GdrCicloModuloEtapaPolicy();
        assertThat(policy.isOperational("cronograma", "BORRADOR")).isTrue();
        assertThat(policy.isAccessible("cronograma", "CERRADO")).isTrue();
    }

    @Test
    void policy_seguimientoNoAccesibleEnBorrador() {
        GdrCicloModuloEtapaPolicy policy = new GdrCicloModuloEtapaPolicy();
        assertThat(policy.evaluate("seguimiento", "BORRADOR"))
                .isEqualTo(GdrCicloModuloEtapaPolicy.AccesoModulo.NOT_APPLICABLE);
        assertThat(policy.isAccessible("seguimiento", "BORRADOR")).isFalse();
    }

    @Test
    void policy_confirmacionSoloDesdeEN_CONFIRMACION() {
        GdrCicloModuloEtapaPolicy policy = new GdrCicloModuloEtapaPolicy();
        assertThat(policy.isOperational("confirmacion", "EN_EVALUACION")).isFalse();
        assertThat(policy.isOperational("confirmacion", "EN_CONFIRMACION")).isTrue();
    }

    @Test
    void policy_slugDesconocidoRetornaNotApplicable() {
        GdrCicloModuloEtapaPolicy policy = new GdrCicloModuloEtapaPolicy();
        assertThat(policy.evaluate("slug-inexistente", "EN_PLANIFICACION"))
                .isEqualTo(GdrCicloModuloEtapaPolicy.AccesoModulo.NOT_APPLICABLE);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void stubChecklistMocks(Long cycleId, boolean full) {
        when(cronogramaRepository.findByCycleIdOrderByFechaInicio(cycleId))
                .thenReturn(fakeEtapas(full ? 7 : 0));
        GdrEvaluationAssignment assignment = fakeAssignment(10L);
        List<GdrEvaluationAssignment> assignments = full ? List.of(assignment) : List.of();
        when(assignmentRepository.findActiveAssignmentsByCycle(cycleId)).thenReturn(assignments);
        if (full) {
            when(assignmentRepository.hasActiveGoals(10L)).thenReturn(true);
            when(goalRepository.findEvaluadosConPesoIncorrectoEnCiclo(cycleId)).thenReturn(List.of());
        }
        when(indicatorRepository.findActive()).thenReturn(
                full ? List.of(mock(pe.gob.gdr.access.domain.model.GdrIndicator.class))
                     : List.of());
        when(cieConformacionRepository.findByCycleIdOrderByVigenciaInicioDesc(cycleId))
                .thenReturn(Collections.emptyList());
    }

    private ActiveCycle cycleEN_PLANIFICACION() {
        return ActiveCycle.builder()
                .id(1L)
                .code("C-2025")
                .name("Ciclo GDR 2025")
                .estadoEtapa("EN_PLANIFICACION")
                .startDate(LocalDate.of(2025, 1, 1))
                .status("ACTIVE")
                .build();
    }

    private List<pe.gob.gdr.access.domain.model.GdrCronograma> fakeEtapas(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> pe.gob.gdr.access.domain.model.GdrCronograma.builder()
                        .id((long) (i + 1))
                        .etapa("ETAPA_" + i)
                        .fechaInicio(LocalDate.of(2025, 1, 1).plusMonths(i))
                        .fechaFin(LocalDate.of(2025, 1, 31).plusMonths(i))
                        .build())
                .toList();
    }

    private GdrEvaluationAssignment fakeAssignment(Long id) {
        return GdrEvaluationAssignment.builder().id(id).build();
    }
}
