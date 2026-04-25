package pe.gob.gdr.access.domain.repository;

import pe.gob.gdr.access.domain.model.DocHash;

public interface DocHashRepository {

    DocHash save(DocHash documentHash);
}
