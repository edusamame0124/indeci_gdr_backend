package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrValueType;

public interface GdrValueTypeRepository {

    List<GdrValueType> findActive();

    Optional<GdrValueType> findActiveById(Long id);
}
