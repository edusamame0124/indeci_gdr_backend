package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrIndicator;

public interface GdrIndicatorRepository {

    List<GdrIndicator> findActive();

    Optional<GdrIndicator> findActiveById(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long excludedId);

    GdrIndicator save(GdrIndicator indicator);
}
