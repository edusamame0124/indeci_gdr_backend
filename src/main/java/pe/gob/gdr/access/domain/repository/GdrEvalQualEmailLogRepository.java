package pe.gob.gdr.access.domain.repository;

import java.util.List;
import pe.gob.gdr.access.domain.model.GdrEvalQualEmailLog;

public interface GdrEvalQualEmailLogRepository {

    GdrEvalQualEmailLog save(GdrEvalQualEmailLog log);

    List<GdrEvalQualEmailLog> findByFinalEvaluation_IdOrderBySentAtDesc(Long finalEvaluationId);
}
