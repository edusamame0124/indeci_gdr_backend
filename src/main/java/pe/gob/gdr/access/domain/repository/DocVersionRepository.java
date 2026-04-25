package pe.gob.gdr.access.domain.repository;

import pe.gob.gdr.access.domain.model.DocVersion;

public interface DocVersionRepository {

    DocVersion save(DocVersion documentVersion);
}
