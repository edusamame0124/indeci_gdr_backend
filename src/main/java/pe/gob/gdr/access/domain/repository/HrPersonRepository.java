package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.HrPerson;

public interface HrPersonRepository {

    Optional<HrPerson> findActiveById(Long id);

    Optional<HrPerson> findEligibleById(Long id);

    Optional<HrPerson> findActiveByDocumentNumber(String documentNumber);

    List<HrPerson> findEligibleForAssignment(String search);

    List<HrPerson> findAllEligibleForAssignment();

    HrPerson save(HrPerson person);
}
