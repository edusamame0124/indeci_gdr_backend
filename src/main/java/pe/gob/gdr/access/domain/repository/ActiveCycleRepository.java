package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.ActiveCycle;

public interface ActiveCycleRepository {

    Optional<ActiveCycle> findActiveCycle();
}
