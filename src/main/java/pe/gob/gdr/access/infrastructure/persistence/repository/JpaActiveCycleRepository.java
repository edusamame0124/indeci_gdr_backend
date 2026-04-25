package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;

@Repository
public interface JpaActiveCycleRepository extends JpaRepository<ActiveCycle, Long>, ActiveCycleRepository {

    List<ActiveCycle> findByStatusOrderByUpdatedAtDescIdDesc(String status);

    @Override
    default Optional<ActiveCycle> findActiveCycle() {
        return findByStatusOrderByUpdatedAtDescIdDesc("ACTIVE").stream().findFirst();
    }
}
