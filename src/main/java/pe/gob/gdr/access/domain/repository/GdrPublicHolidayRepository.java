package pe.gob.gdr.access.domain.repository;

import java.time.LocalDate;
import java.util.Set;

public interface GdrPublicHolidayRepository {

    Set<LocalDate> findHolidayDatesBetween(LocalDate fromInclusive, LocalDate toInclusive);
}
