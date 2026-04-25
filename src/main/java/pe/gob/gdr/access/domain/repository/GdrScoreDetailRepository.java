package pe.gob.gdr.access.domain.repository;

import java.util.List;
import pe.gob.gdr.access.domain.model.GdrScoreDetail;

public interface GdrScoreDetailRepository {

    List<GdrScoreDetail> findByFinalEvaluationId(Long finalEvaluationId);

    void deleteByFinalEvaluationId(Long finalEvaluationId);

    <S extends GdrScoreDetail> List<S> saveAll(Iterable<S> details);
}
