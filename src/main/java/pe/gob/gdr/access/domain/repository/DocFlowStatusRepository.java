package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.DocFlowStatus;

public interface DocFlowStatusRepository {

    Optional<DocFlowStatus> findActiveByCode(String code);
}
