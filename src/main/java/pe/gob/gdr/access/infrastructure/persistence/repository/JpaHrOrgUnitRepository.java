package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.repository.HrOrgUnitRepository;

@Repository
public interface JpaHrOrgUnitRepository extends JpaRepository<HrOrgUnit, Long>, HrOrgUnitRepository {

    @Override
    @Query("""
            select orgUnit
            from HrOrgUnit orgUnit
            where upper(orgUnit.status) = 'ACTIVE'
            order by orgUnit.name asc
            """)
    List<HrOrgUnit> findAllActiveOrdered();

    @Override
    @EntityGraph(attributePaths = {"parent"})
    @Query(
            value = """
                    select orgUnit
                    from HrOrgUnit orgUnit
                    where upper(orgUnit.status) = 'ACTIVE'
                    order by orgUnit.displayOrder asc, orgUnit.name asc
                    """,
            countQuery = """
                    select count(orgUnit)
                    from HrOrgUnit orgUnit
                    where upper(orgUnit.status) = 'ACTIVE'
                    """
    )
    Page<HrOrgUnit> findPageActiveOrganigrama(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"parent"})
    @Query("""
            select orgUnit
            from HrOrgUnit orgUnit
            where upper(orgUnit.status) = 'ACTIVE'
              and (
                    substring(orgUnit.code, 1, 3) = 'OF_'
                    or orgUnit.code = 'OTI'
                  )
            order by orgUnit.displayOrder asc, orgUnit.name asc
            """)
    List<HrOrgUnit> findAllActiveOfficesForOrganigrama();

    @Override
    @Query("""
            select orgUnit
            from HrOrgUnit orgUnit
            where orgUnit.id = :id
              and upper(orgUnit.status) = 'ACTIVE'
            """)
    Optional<HrOrgUnit> findActiveById(@Param("id") Long id);
}
