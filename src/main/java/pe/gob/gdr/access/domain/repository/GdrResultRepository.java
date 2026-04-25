package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrResult;

public interface GdrResultRepository {

    List<GdrResult> findAllInActiveCycle();

    Optional<GdrResult> findByAssignmentIdInActiveCycle(Long assignmentId);

    Optional<GdrResult> findByEvaluatedPersonIdInActiveCycle(Long evaluatedId);

    GdrResult save(GdrResult result);
}
