package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.response.ResultadoResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
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
    public GdrResult syncResult(GdrEvaluationAssignment assignment, GdrFinalEvaluation evaluation, BigDecimal consolidatedScore) {
        GdrResult result = resultRepository.findByAssignmentIdInActiveCycle(assignment.getId())
                .orElseGet(GdrResult::new);
        result.setAssignment(assignment);
        result.setFinalEvaluation(evaluation);
        result.setConsolidatedScore(consolidatedScore);
        result.setStatus("ACTIVE");
        return resultRepository.save(result);
    }

    private ResultadoResponse mapResponse(GdrResult result) {
        return new ResultadoResponse(
                result.getId(),
                result.getAssignment().getId(),
                result.getAssignment().getEvaluatedPerson().getId(),
                result.getAssignment().getEvaluatedPerson().getDisplayName(),
                result.getAssignment().getEvaluatorPerson().getDisplayName(),
                result.getAssignment().getCycle().getName(),
                result.getConsolidatedScore(),
                result.getStatus()
        );
    }
}
