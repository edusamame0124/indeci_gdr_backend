package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrFormula;
import pe.gob.gdr.access.domain.repository.GdrFormulaRepository;

@Repository
public interface JpaGdrFormulaRepository extends JpaRepository<GdrFormula, Long>, GdrFormulaRepository {

    List<GdrFormula> findByStatusOrderByNameAsc(String status);

    Optional<GdrFormula> findByIdAndStatus(Long id, String status);

    @Override
    default List<GdrFormula> findActive() {
        return findByStatusOrderByNameAsc("ACTIVE");
    }

    @Override
    default Optional<GdrFormula> findActiveById(Long id) {
        return findByIdAndStatus(id, "ACTIVE");
    }
}
