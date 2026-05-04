package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.response.ResultadoResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

@Service
public class GdrResultService {

    private final GdrResultRepository resultRepository;

    public GdrResultService(GdrResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public ResultadoResponse getResultByEvaluatedId(Long evaluatedId) {
        GdrResult result = resultRepository.findByEvaluatedPersonIdInActiveCycle(evaluatedId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro un resultado consolidado para el evaluado."));
        return mapResponse(result);
    }

    @Transactional
    public GdrResult syncResult(
            GdrEvaluationAssignment assignment,
            GdrFinalEvaluation evaluation,
            BigDecimal consolidatedScore,
            String qualitativeRatingCode
    ) {
        GdrResult result = resultRepository.findByAssignmentIdInActiveCycle(assignment.getId())
                .orElseGet(GdrResult::new);
        result.setAssignment(assignment);
        result.setFinalEvaluation(evaluation);
        result.setConsolidatedScore(consolidatedScore);
        result.setQualitativeRatingCode(qualitativeRatingCode);
        result.setStatus("ACTIVE");
        return resultRepository.save(result);
    }

    private ResultadoResponse mapResponse(GdrResult result) {
        GdrSegment segment = result.getAssignment().getSegment();
        return new ResultadoResponse(
                result.getId(),
                result.getAssignment().getId(),
                result.getAssignment().getEvaluatedPerson().getId(),
                result.getAssignment().getEvaluatedPerson().getDisplayName(),
                result.getAssignment().getEvaluatorPerson().getDisplayName(),
                result.getAssignment().getCycle().getName(),
                result.getConsolidatedScore(),
                result.getQualitativeRatingCode(),
                QualitativeRating.labelOf(result.getQualitativeRatingCode()),
                segment != null ? segment.getCode() : null,
                segment != null ? segment.getName() : null,
                result.getStatus()
        );
    }
}
