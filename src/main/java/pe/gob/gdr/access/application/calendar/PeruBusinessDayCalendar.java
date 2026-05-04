package pe.gob.gdr.access.application.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Peru calendar helpers: Saturday/Sunday non-working; public holidays passed explicitly (e.g. from GDR_PUBLIC_HOLIDAY).
 */
public final class PeruBusinessDayCalendar {

    private PeruBusinessDayCalendar() {
    }

    /** Five business days after final grade window end (first counted day = first business day strictly after anchor). */
    public static final int NOTIFY_BUSINESS_DAYS_AFTER_GRADE_END = 5;

    public static boolean isBusinessDay(LocalDate date, Set<LocalDate> holidays) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        return holidays == null || !holidays.contains(date);
    }

    /**
     * @param anchorLastGradeDay last calendar day of grading window (inclusive for evaluators)
     * @return last calendar day of the notification window (inclusive)
     */
    public static LocalDate fifthBusinessDayAfterGradeEnd(LocalDate anchorLastGradeDay, Set<LocalDate> holidays) {
        LocalDate d = anchorLastGradeDay.plusDays(1);
        d = advanceToBusinessDay(d, holidays);
        for (int i = 1; i < NOTIFY_BUSINESS_DAYS_AFTER_GRADE_END; i++) {
            d = advanceToBusinessDay(d.plusDays(1), holidays);
        }
        return d;
    }

    private static LocalDate advanceToBusinessDay(LocalDate date, Set<LocalDate> holidays) {
        LocalDate d = date;
        while (!isBusinessDay(d, holidays)) {
            d = d.plusDays(1);
        }
        return d;
    }
}
