package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;

@ExtendWith(MockitoExtension.class)
class GdrCicloEstadoServiceTest {

    @Mock ActiveCycleRepository activeCycleRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepository;
    @Mock GdrCasoCieRepository casoCieRepository;
    @Mock AuditTrailService auditTrailService;

    private GdrValidacionNormativaService validacion;
    private GdrCicloEstadoService sut;

    @BeforeEach
    void setUp() {
        validacion = new GdrValidacionNormativaService(
                finalEvaluationRepository, assignmentRepository, casoCieRepository);
        sut = new GdrCicloEstadoService(activeCycleRepository, validacion, auditTrailService);
    }

    // ── transicionesDisponibles ────────────────────────────────────────────

    @Test
    void transicionesDisponibles_borradorPuedeAvanzarAPlanificacion() {
        assertThat(sut.transicionesDisponibles(GdrCicloEstadoService.BORRADOR))
                .containsExactly(GdrCicloEstadoService.EN_PLANIFICACION);
    }

    @Test
    void transicionesDisponibles_cerradoNoTieneTransiciones() {
        assertThat(sut.transicionesDisponibles(GdrCicloEstadoService.CERRADO)).isEmpty();
    }

    @Test
    void transicionesDisponibles_anuladoNoTieneTransiciones() {
        assertThat(sut.transicionesDisponibles(GdrCicloEstadoService.ANULADO)).isEmpty();
    }

    // ── avanzarEtapa — caso feliz ──────────────────────────────────────────

    @Test
    void avanzarEtapa_borradorAvanzaAEnPlanificacion() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.BORRADOR, LocalDate.of(2025, 1, 1));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(activeCycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActiveCycle result = sut.avanzarEtapa(1L);

        assertThat(result.getEstadoEtapa()).isEqualTo(GdrCicloEstadoService.EN_PLANIFICACION);
    }

    @Test
    void avanzarEtapa_planificacionAvanzaASeguimiento_conFechaSeguimientoSuficiente() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.EN_PLANIFICACION, LocalDate.of(2025, 1, 1));
        // fechaFinSeguimiento = 6 meses después del inicio
        cycle.setFechaFinSeguimiento(LocalDate.of(2025, 7, 15));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(activeCycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActiveCycle result = sut.avanzarEtapa(1L);

        assertThat(result.getEstadoEtapa()).isEqualTo(GdrCicloEstadoService.EN_SEGUIMIENTO);
    }

    @Test
    void avanzarEtapa_planificacionFalla_siSeguimientoMenor6Meses() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.EN_PLANIFICACION, LocalDate.of(2025, 1, 1));
        // solo 2 meses de seguimiento — falla VAL-01
        cycle.setFechaFinSeguimiento(LocalDate.of(2025, 3, 1));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> sut.avanzarEtapa(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("menor al mínimo normativo");
    }

    @Test
    void avanzarEtapa_sinFechasSeguimiento_lanzaError() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.EN_PLANIFICACION, LocalDate.of(2025, 1, 1));
        // fechaFinSeguimiento = null
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> sut.avanzarEtapa(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Configure el cronograma");
    }

    @Test
    void avanzarEtapa_cerradoLanzaError() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.CERRADO, LocalDate.of(2025, 1, 1));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> sut.avanzarEtapa(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no puede avanzar desde el estado");
    }

    @Test
    void avanzarEtapa_cicloInexistente_lanzaResourceNotFound() {
        when(activeCycleRepository.findByIdForAdministration(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.avanzarEtapa(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── anular ────────────────────────────────────────────────────────────

    @Test
    void anular_cicloActivoQuedaAnulado() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.EN_SEGUIMIENTO, LocalDate.of(2025, 1, 1));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));
        when(activeCycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActiveCycle result = sut.anular(1L);

        assertThat(result.getEstadoEtapa()).isEqualTo(GdrCicloEstadoService.ANULADO);
    }

    @Test
    void anular_cicloYaCerradoLanzaError() {
        ActiveCycle cycle = cycleWithEstado(GdrCicloEstadoService.CERRADO, LocalDate.of(2025, 1, 1));
        when(activeCycleRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> sut.anular(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ya cerrado o anulado");
    }

    // ── labelFor ──────────────────────────────────────────────────────────

    @Test
    void labelFor_retornaEtiquetaLegible() {
        assertThat(sut.labelFor(GdrCicloEstadoService.EN_PLANIFICACION))
                .isEqualTo("En planificación");
        assertThat(sut.labelFor(GdrCicloEstadoService.CERRADO))
                .isEqualTo("Cerrado");
    }

    // ── helper ────────────────────────────────────────────────────────────

    private ActiveCycle cycleWithEstado(String estado, LocalDate startDate) {
        return ActiveCycle.builder()
                .id(1L)
                .code("C-2025")
                .name("Ciclo 2025")
                .estadoEtapa(estado)
                .startDate(startDate)
                .status("ACTIVE")
                .build();
    }
}
