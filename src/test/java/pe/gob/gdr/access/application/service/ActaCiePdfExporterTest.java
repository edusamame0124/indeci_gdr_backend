package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

class ActaCiePdfExporterTest {

    private ActaCiePdfExporter exporter;

    @BeforeEach
    void setUp() {
        FormatoGdrPdfProperties properties = new FormatoGdrPdfProperties();
        properties.setEntityName("INDECI");
        exporter = new ActaCiePdfExporter(properties);
    }

    @Test
    void exportPdfCasoResueltoProducesPdf() {
        byte[] pdf = exporter.exportPdf(casoResuelto());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
    }

    @Test
    void exportPdfCasoPendienteLanzaError() {
        GdrCasoCie caso = casoResuelto();
        caso.setEstado(GdrCasoCie.ESTADO_RECIBIDO);

        assertThatThrownBy(() -> exporter.exportPdf(caso))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("casos resueltos");
    }

    @Test
    void exportPdfModificaIncluyeNuevaCalificacion() {
        GdrCasoCie caso = casoResuelto();
        caso.setDecision(GdrCasoCie.DECISION_MODIFICA);
        caso.setCalificacionResultado("BUEN_RENDIMIENTO");

        byte[] pdf = exporter.exportPdf(caso);

        assertThat(pdf).isNotEmpty();
    }

    private static GdrCasoCie casoResuelto() {
        HrPerson evaluado = HrPerson.builder().id(2L).displayName("SERVIDOR PRUEBA").build();
        HrPerson evaluador = HrPerson.builder().id(3L).displayName("JEFE PRUEBA").build();
        ActiveCycle cycle = ActiveCycle.builder()
                .id(1L).name("Ciclo 2026")
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .id(1L).cycle(cycle).evaluatedPerson(evaluado).evaluatorPerson(evaluador)
                .build();
        GdrFinalEvaluation evaluation = GdrFinalEvaluation.builder()
                .id(50L)
                .assignment(assignment)
                .consolidatedScore(new BigDecimal("80"))
                .qualitativeRatingCode("SUJETO_OBSERVACION")
                .build();
        GdrSolicitudConfirmacion solicitud = GdrSolicitudConfirmacion.builder()
                .id(7L)
                .finalEvaluation(evaluation)
                .evaluado(evaluado)
                .cycle(cycle)
                .sustentoEvaluado("Disconformidad con la calificación.")
                .build();
        return GdrCasoCie.builder()
                .id(10L)
                .solicitud(solicitud)
                .numeroCaso("CIE-2026-0001")
                .fechaIngresoCie(LocalDateTime.now().minusDays(2))
                .estado(GdrCasoCie.ESTADO_RESUELTO)
                .decision(GdrCasoCie.DECISION_CONFIRMA)
                .sustentoCie("Se confirma la calificación.")
                .fechaDecision(LocalDateTime.now())
                .build();
    }
}
