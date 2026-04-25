package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrValueType;
import pe.gob.gdr.access.domain.repository.GdrValueTypeRepository;

@Repository
public interface JpaGdrValueTypeRepository extends JpaRepository<GdrValueType, Long>, GdrValueTypeRepository {

    List<GdrValueType> findByStatusOrderByNameAsc(String status);

    Optional<GdrValueType> findByIdAndStatus(Long id, String status);

    @Override
    default List<GdrValueType> findActive() {
        return findByStatusOrderByNameAsc("ACTIVE");
    }

    @Override
    default Optional<GdrValueType> findActiveById(Long id) {
        return findByIdAndStatus(id, "ACTIVE");
    }
}
