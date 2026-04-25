package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;

public interface GdrFinalEvaluationRepository {

    List<GdrFinalEvaluation> findActiveFinalEvaluationsForActiveCycle();

    Optional<GdrFinalEvaluation> findByAssignmentIdInActiveCycle(Long assignmentId);

    Optional<GdrFinalEvaluation> findByIdInActiveCycle(Long evaluationId);

    Optional<GdrFinalEvaluation> findByEvaluatedPersonIdInActiveCycle(Long evaluatedId);

    GdrFinalEvaluation save(GdrFinalEvaluation evaluation);
}

