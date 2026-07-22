package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;

class GdrValidacionNormativaServiceTest {

    private final GdrValidacionNormativaService sut = new GdrValidacionNormativaService(
            Mockito.mock(GdrFinalEvaluationRepository.class),
            Mockito.mock(GdrEvaluationAssignmentRepository.class),
            Mockito.mock(GdrCasoCieRepository.class));

    @Test
    void calcularFechaLimiteInforme_retorna31MayoAnioSiguiente() {
        ActiveCycle cycle = new ActiveCycle();
        cycle.setStartDate(LocalDate.of(2025, 3, 15));

        LocalDate limite = sut.calcularFechaLimiteInforme(cycle);

        assertThat(limite).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    void calcularFechaLimiteInforme_sinFechaInicio_retornaNull() {
        ActiveCycle cycle = new ActiveCycle();

        assertThat(sut.calcularFechaLimiteInforme(cycle)).isNull();
    }

    @Test
    void calcularFechaLimiteInforme_ciclo2024_inicia2025() {
        ActiveCycle cycle = new ActiveCycle();
        cycle.setStartDate(LocalDate.of(2024, 12, 1));

        assertThat(sut.calcularFechaLimiteInforme(cycle)).isEqualTo(LocalDate.of(2025, 5, 31));
    }
}
