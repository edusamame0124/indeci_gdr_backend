package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.gdr.access.domain.model.GdrPublicHoliday;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;

public interface JpaGdrPublicHolidayRepository extends JpaRepository<GdrPublicHoliday, Long>, GdrPublicHolidayRepository {

    @Query("""
            select h.holidayDate from GdrPublicHoliday h
            where h.holidayDate between :fromInclusive and :toInclusive
            """)
    List<LocalDate> findHolidayDateList(
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive
    );

    @Override
    default Set<LocalDate> findHolidayDatesBetween(LocalDate fromInclusive, LocalDate toInclusive) {
        return new HashSet<>(findHolidayDateList(fromInclusive, toInclusive));
    }
}
