package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrSegment;

public interface GdrSegmentRepository {

    List<GdrSegment> findActive();

    Optional<GdrSegment> findActiveById(Long id);

    Optional<GdrSegment> findActiveByCode(String code);
}
