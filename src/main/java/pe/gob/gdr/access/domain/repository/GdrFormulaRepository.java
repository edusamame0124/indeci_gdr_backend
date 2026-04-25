package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrFormula;

public interface GdrFormulaRepository {

    List<GdrFormula> findActive();

    Optional<GdrFormula> findActiveById(Long id);
}
