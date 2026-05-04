package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Override
    @Query("""
            select cycle
            from ActiveCycle cycle
            order by case when upper(cycle.status) = 'ACTIVE' then 0 else 1 end asc,
                     cycle.startDate desc,
                     cycle.id desc
            """)
    List<ActiveCycle> findAllOrderedForAdministration();

    @Override
    @Query("""
            select cycle
            from ActiveCycle cycle
            where cycle.id = :cycleId
            """)
    Optional<ActiveCycle> findByIdForAdministration(@Param("cycleId") Long cycleId);
}
