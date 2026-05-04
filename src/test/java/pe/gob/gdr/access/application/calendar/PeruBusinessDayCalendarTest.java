package pe.gob.gdr.access.application.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PeruBusinessDayCalendarTest {

    @Test
    void isBusinessDay_excludesWeekend() {
        assertFalse(PeruBusinessDayCalendar.isBusinessDay(LocalDate.of(2026, 1, 10), Set.of()));
        assertTrue(PeruBusinessDayCalendar.isBusinessDay(LocalDate.of(2026, 1, 12), Set.of()));
    }

    @Test
    void isBusinessDay_excludesHoliday() {
        LocalDate holiday = LocalDate.of(2026, 1, 1);
        assertFalse(PeruBusinessDayCalendar.isBusinessDay(holiday, Set.of(holiday)));
    }

    @Test
    void fifthBusinessDayAfterGradeEnd_countsFromFirstBusinessDayAfterAnchor() {
        LocalDate gradeEndFriday = LocalDate.of(2026, 1, 9);
        LocalDate expected = LocalDate.of(2026, 1, 16);
        assertEquals(expected, PeruBusinessDayCalendar.fifthBusinessDayAfterGradeEnd(gradeEndFriday, Set.of()));
    }
}
