package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrIndicator;
import pe.gob.gdr.access.domain.repository.GdrIndicatorRepository;

@Repository
public interface JpaGdrIndicatorRepository extends JpaRepository<GdrIndicator, Long>, GdrIndicatorRepository {

    @Override
    @Query("""
            select indicator
            from GdrIndicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where upper(indicator.status) = 'ACTIVE'
            order by indicator.name asc
            """)
    List<GdrIndicator> findActive();

    @Override
    @Query("""
            select indicator
            from GdrIndicator indicator
            join fetch indicator.valueType valueType
            join fetch indicator.formula formula
            join fetch indicator.segment segment
            where indicator.id = :id
              and upper(indicator.status) = 'ACTIVE'
            """)
    Optional<GdrIndicator> findActiveById(@Param("id") Long id);

    @Override
    boolean existsByCodeIgnoreCase(String code);

    @Override
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long excludedId);
}
