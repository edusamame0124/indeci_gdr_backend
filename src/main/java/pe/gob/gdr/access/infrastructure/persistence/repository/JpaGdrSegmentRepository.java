package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.repository.GdrSegmentRepository;

@Repository
public interface JpaGdrSegmentRepository extends JpaRepository<GdrSegment, Long>, GdrSegmentRepository {

    List<GdrSegment> findByStatusOrderByNameAsc(String status);

    Optional<GdrSegment> findByIdAndStatus(Long id, String status);

    Optional<GdrSegment> findByCodeAndStatus(String code, String status);

    @Override
    default List<GdrSegment> findActive() {
        return findByStatusOrderByNameAsc("ACTIVE");
    }

    @Override
    default Optional<GdrSegment> findActiveById(Long id) {
        return findByIdAndStatus(id, "ACTIVE");
    }

    @Override
    default Optional<GdrSegment> findActiveByCode(String code) {
        return findByCodeAndStatus(code, "ACTIVE");
    }
}
