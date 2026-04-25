package pe.gob.gdr.access.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocHash;
import pe.gob.gdr.access.domain.repository.DocHashRepository;

@Repository
public interface JpaDocHashRepository extends JpaRepository<DocHash, Long>, DocHashRepository {
}
