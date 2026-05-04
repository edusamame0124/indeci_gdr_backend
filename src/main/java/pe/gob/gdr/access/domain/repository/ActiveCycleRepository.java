package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.ActiveCycle;

public interface ActiveCycleRepository {

    Optional<ActiveCycle> findActiveCycle();

    List<ActiveCycle> findAllOrderedForAdministration();

    Optional<ActiveCycle> findByIdForAdministration(Long cycleId);
}
