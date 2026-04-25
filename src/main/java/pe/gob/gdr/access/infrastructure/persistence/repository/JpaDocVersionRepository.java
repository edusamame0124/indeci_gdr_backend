package pe.gob.gdr.access.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocVersion;
import pe.gob.gdr.access.domain.repository.DocVersionRepository;

@Repository
public interface JpaDocVersionRepository extends JpaRepository<DocVersion, Long>, DocVersionRepository {
}
