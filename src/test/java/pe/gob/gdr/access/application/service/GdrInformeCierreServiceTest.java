package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.request.GenerarInformeCierreRequest;
import pe.gob.gdr.access.application.dto.response.InformeCierreAlertaResponse;
import pe.gob.gdr.access.application.dto.response.InformeCierreConsolidadoResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrInformeCierre;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRepository;

@ExtendWith(MockitoExtension.class)
class GdrInformeCierreServiceTest {

    @Mock ActiveCycleRepository activeCycleRepository;
    @Mock GdrInformeCierreRepository informeRepository;
    @Mock GdrInformeCierreConsolidador consolidador;
    @Mock GdrValidacionNormativaService validacionNormativaService;
    @Mock FormatoInformeCierrePdfExporter pdfExporter;
    @Mock AuditTrailService auditTrailService;

    private GdrInformeCierreService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrInformeCierreService(
                activeCycleRepository,
                informeRepository,
                consolidador,
                validacionNormativaService,
                pdfExporter,
                auditTrailService
        );
    }

    @Test
    void obtenerVistaPrevia_consolidaCicloActivo() {
        ActiveCycle cycle = cycleActivo();
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(consolidador.consolidar(cycle)).thenReturn(snapshot());

        InformeCierreConsolidadoResponse response = sut.obtenerVistaPrevia();

        assertThat(response.cycleId()).isEqualTo(1L);
        assertThat(response.totalEvaluados()).isEqualTo(10);
        assertThat(response.totalBuenRendimiento()).isEqualTo(6);
        assertThat(response.informeId()).isNull();
    }

    @Test
    void obtenerAlertaVal06_retornaSemaforoCriticoCuandoVencida() {
        ActiveCycle cycle = cycleActivo();
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(validacionNormativaService.calcularFechaLimiteInforme(cycle))
                .thenReturn(LocalDate.now().minusDays(1));

        InformeCierreAlertaResponse alerta = sut.obtenerAlertaVal06();

        assertThat(alerta.vencida()).isTrue();
        assertThat(alerta.nivelSemaforo()).isEqualTo("CRITICO");
        assertThat(alerta.diasRestantes()).isZero();
    }

    @Test
    void generar_persisteBorradorYRegistraAuditoria() {
        ActiveCycle cycle = cycleActivo();
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(consolidador.consolidar(cycle)).thenReturn(snapshot());
        when(informeRepository.save(any())).thenAnswer(inv -> {
            GdrInformeCierre informe = inv.getArgument(0);
            informe.setId(99L);
            return informe;
        });

        InformeCierreConsolidadoResponse response = sut.generar(
                new GenerarInformeCierreRequest("Observaciones de cierre."),
                "orh.gdr"
        );

        assertThat(response.informeId()).isEqualTo(99L);
        assertThat(response.estado()).isEqualTo(GdrInformeCierre.ESTADO_BORRADOR);
        assertThat(response.generadoPor()).isEqualTo("orh.gdr");
        verify(auditTrailService).recordEvent(
                eq("INFORME_CIERRE_GENERADO"),
                eq("orh.gdr"),
                any(),
                eq(null)
        );
    }

    @Test
    void listarHistorial_sinCicloActivo_lanzaDomainException() {
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.listarHistorial())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ciclo activo");
    }

    @Test
    void listarHistorial_retornaInformesDelCiclo() {
        ActiveCycle cycle = cycleActivo();
        GdrInformeCierre informe = GdrInformeCierre.builder()
                .id(5L)
                .cycle(cycle)
                .estado(GdrInformeCierre.ESTADO_BORRADOR)
                .totalEvaluados(10)
                .totalBuenRendimiento(6)
                .totalSujetoObservacion(2)
                .totalDesaprobado(1)
                .totalDistinguido(1)
                .totalOportunidadesMejora(3)
                .totalConfirmaciones(2)
                .totalConfirmacionesResueltas(1)
                .totalDocumentosFirmados(8)
                .generadoPor("orh.gdr")
                .fechaGeneracion(LocalDateTime.now())
                .build();
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(informeRepository.findByCycleIdOrderByFechaGeneracionDesc(1L)).thenReturn(List.of(informe));

        List<InformeCierreConsolidadoResponse> historial = sut.listarHistorial();

        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().informeId()).isEqualTo(5L);
    }

    private ActiveCycle cycleActivo() {
        ActiveCycle cycle = new ActiveCycle();
        cycle.setId(1L);
        cycle.setCode("GDR-2025");
        cycle.setName("Ciclo GDR 2025");
        cycle.setStartDate(LocalDate.of(2025, 1, 1));
        return cycle;
    }

    private GdrInformeCierreConsolidador.InformeCierreSnapshot snapshot() {
        return new GdrInformeCierreConsolidador.InformeCierreSnapshot(
                10, 6, 2, 1, 1, 3, 2, 1, 8
        );
    }
}
