package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.DocType;

public interface DocTypeRepository {

    List<DocType> findActiveTypes();

    Optional<DocType> findActiveById(Long typeId);

    Optional<DocType> findActiveByCode(String code);
}
