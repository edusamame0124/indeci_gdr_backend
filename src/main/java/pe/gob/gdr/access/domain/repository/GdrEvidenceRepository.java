package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvidence;

public interface GdrEvidenceRepository {

    List<GdrEvidence> findActiveForActiveCycle();

    List<GdrEvidence> findActiveByGoalIdInActiveCycle(Long goalId);

    Optional<GdrEvidence> findActiveByIdInActiveCycle(Long evidenceId);

    List<GdrEvidence> findActiveByGoalAssignmentIdInActiveCycle(Long assignmentId);

    GdrEvidence save(GdrEvidence evidence);
}
