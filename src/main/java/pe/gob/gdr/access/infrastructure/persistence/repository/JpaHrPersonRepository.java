package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.HrPersonRepository;

@Repository
public interface JpaHrPersonRepository extends JpaRepository<HrPerson, Long>, HrPersonRepository {

    @Override
    @Query("""
            select person
            from HrPerson person
            join fetch person.orgUnit orgUnit
            where person.id = :id
              and upper(person.status) = 'ACTIVE'
            """)
    Optional<HrPerson> findActiveById(@Param("id") Long id);

    @Override
    @Query("""
            select person
            from HrPerson person
            join fetch person.orgUnit orgUnit
            where upper(person.documentNumber) = upper(:documentNumber)
              and upper(person.status) = 'ACTIVE'
            """)
    Optional<HrPerson> findActiveByDocumentNumber(@Param("documentNumber") String documentNumber);

    @Override
    @Query("""
            select person
            from HrPerson person
            join fetch person.orgUnit orgUnit
            where person.id = :id
              and upper(person.status) = 'ACTIVE'
              and exists (
                  select 1
                  from User u
                  join u.userRoles ur
                  join ur.role r
                  where u.person.id = person.id
                    and upper(u.status) = 'ACTIVE'
                    and upper(ur.status) = 'ACTIVE'
                    and upper(r.status) = 'ACTIVE'
                    and upper(r.code) = 'GDR_USUARIO'
              )
            """)
    Optional<HrPerson> findEligibleById(@Param("id") Long id);

    @Override
    @Query("""
            select distinct person
            from HrPerson person
            join fetch person.orgUnit orgUnit
            where upper(person.status) = 'ACTIVE'
              and exists (
                  select 1
                  from User u
                  join u.userRoles ur
                  join ur.role r
                  where u.person.id = person.id
                    and upper(u.status) = 'ACTIVE'
                    and upper(ur.status) = 'ACTIVE'
                    and upper(r.status) = 'ACTIVE'
                    and upper(r.code) = 'GDR_USUARIO'
              )
              and (:search is null
                   or lower(person.displayName) like lower(concat('%', :search, '%'))
                   or person.documentNumber like concat(:search, '%')
                   or lower(orgUnit.name) like lower(concat('%', :search, '%')))
            order by person.displayName asc
            """)
    List<HrPerson> findEligibleForAssignment(@Param("search") String search);

    @Override
    @Query("""
            select distinct person
            from HrPerson person
            join fetch person.orgUnit orgUnit
            where upper(person.status) = 'ACTIVE'
              and exists (
                  select 1
                  from User u
                  join u.userRoles ur
                  join ur.role r
                  where u.person.id = person.id
                    and upper(u.status) = 'ACTIVE'
                    and upper(ur.status) = 'ACTIVE'
                    and upper(r.status) = 'ACTIVE'
                    and upper(r.code) = 'GDR_USUARIO'
              )
            order by person.displayName asc
            """)
    List<HrPerson> findAllEligibleForAssignment();
}
