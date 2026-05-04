package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.HrOrgUnit;

public interface HrOrgUnitRepository {

    List<HrOrgUnit> findAllActiveOrdered();

    Optional<HrOrgUnit> findActiveById(Long id);
}
