package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrFormula;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrIndicator;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.model.GdrValueType;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

@ExtendWith(MockitoExtension.class)
class FormatoGdrPdfExporterTest {

    @Mock
    private GdrGoalRepository goalRepository;

    @Mock
    private GdrEvidenceRepository evidenceRepository;

    @Mock
    private GdrImprovementOpportunityRepository improvementRepository;

    private FormatoGdrPdfExporter exporter;

    @BeforeEach
    void setUp() {
        FormatoGdrPdfProperties properties = new FormatoGdrPdfProperties();
        properties.setEntityName("Entidad de prueba");
        exporter = new FormatoGdrPdfExporter(
                properties,
                goalRepository,
                evidenceRepository,
                improvementRepository
        );
    }

    @Test
    void exportPdfProducesNonEmptyPdfWithOneGoalTwoEvidences() {
        GdrResult result = minimalResult();
        GdrGoal goal = minimalGoal(result.getAssignment());
        GdrEvidence e1 = minimalEvidence(goal, 1L, "Informe uno");
        GdrEvidence e2 = minimalEvidence(goal, 2L, "Informe dos");

        when(goalRepository.findActiveGoalsByAssignmentIdInActiveCycle(70L)).thenReturn(List.of(goal));
        when(evidenceRepository.findActiveByGoalIdInActiveCycle(goal.getId())).thenReturn(List.of(e1, e2));
        when(evidenceRepository.findActiveByGoalAssignmentIdInActiveCycle(70L)).thenReturn(List.of(e1, e2));
        when(improvementRepository.findActiveByEvaluatedIdInActiveCycle(500L)).thenReturn(List.of());

        byte[] pdf = exporter.exportPdf(new FormatoGdrPdfExportContext(result.getAssignment(), Optional.of(result)));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
    }

    @Test
    void exportPdfDraftWithoutConsolidatedResultProducesPdf() {
        GdrResult shell = minimalResult();
        GdrEvaluationAssignment assignment = shell.getAssignment();

        when(goalRepository.findActiveGoalsByAssignmentIdInActiveCycle(70L)).thenReturn(List.of());
        when(evidenceRepository.findActiveByGoalAssignmentIdInActiveCycle(70L)).thenReturn(List.of());
        when(improvementRepository.findActiveByEvaluatedIdInActiveCycle(500L)).thenReturn(List.of());

        byte[] pdf = exporter.exportPdf(new FormatoGdrPdfExportContext(assignment, Optional.empty()));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(5, pdf.length))).startsWith("%PDF-");
    }

    private static GdrResult minimalResult() {
        HrOrgUnit org = new HrOrgUnit();
        org.setName("Oficina de informatica");

        HrPerson evaluated = new HrPerson();
        evaluated.setId(500L);
        evaluated.setDocumentNumber("71619257");
        evaluated.setDisplayName("CONDORI CASTILLO EDWIN");
        evaluated.setOrgUnit(org);

        HrPerson evaluator = new HrPerson();
        evaluator.setId(501L);
        evaluator.setDocumentNumber("28308247");
        evaluator.setDisplayName("GUZMAN FUCHS MAY");
        evaluator.setOrgUnit(org);

        GdrSegment segment = new GdrSegment();
        segment.setCode("EJEC");
        segment.setName("EJECUTOR");

        ActiveCycle cycle = ActiveCycle.builder()
                .id(9L)
                .code("2026")
                .name("Ciclo 2026")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .id(70L)
                .cycle(cycle)
                .evaluatedPerson(evaluated)
                .evaluatorPerson(evaluator)
                .segment(segment)
                .build();

        GdrFinalEvaluation finalEval = GdrFinalEvaluation.builder()
                .assignment(assignment)
                .consolidatedScore(new BigDecimal("85.5"))
                .qualitativeRatingCode("BUEN_RENDIMIENTO")
                .build();

        return GdrResult.builder()
                .assignment(assignment)
                .finalEvaluation(finalEval)
                .consolidatedScore(new BigDecimal("85.5"))
                .qualitativeRatingCode("BUEN_RENDIMIENTO")
                .directive82Compliance("Y")
                .build();
    }

    private static GdrGoal minimalGoal(GdrEvaluationAssignment assignment) {
        GdrValueType vt = new GdrValueType();
        vt.setCode("NUMERIC");
        GdrFormula formula = new GdrFormula();
        formula.setCode("ASC");
        formula.setName("Ascendente");
        GdrIndicator indicator = new GdrIndicator();
        indicator.setName("Indicador prueba");
        indicator.setValueType(vt);
        indicator.setFormula(formula);
        GdrSegment segmentInd = new GdrSegment();
        segmentInd.setCode("SEG");
        segmentInd.setName("SEG");
        indicator.setSegment(segmentInd);
        GdrGoal goal = new GdrGoal();
        goal.setId(900L);
        goal.setTitle("Prioridad anual de prueba");
        goal.setAssignment(assignment);
        goal.setIndicator(indicator);
        goal.setExpectedValue(new BigDecimal("100"));
        goal.setWeight(new BigDecimal("100"));
        goal.setAchievedValue(new BigDecimal("95"));
        goal.setCalculatedScore(new BigDecimal("95"));
        goal.setStartDate(LocalDate.of(2026, 1, 1));
        goal.setEndDate(LocalDate.of(2026, 12, 31));
        return goal;
    }

    private static GdrEvidence minimalEvidence(GdrGoal goal, Long id, String title) {
        GdrEvidenceStatus st = new GdrEvidenceStatus();
        st.setStatusCode("APPROVED");
        GdrEvidence e = new GdrEvidence();
        e.setId(id);
        e.setGoal(goal);
        e.setTitle(title);
        e.setEvidenceTypeCode("INFORME");
        e.setExpectedFormatCode("PDF");
        e.setEvidenceStatus(st);
        e.setExpectedDate(LocalDate.of(2026, 9, 30));
        return e;
    }
}
