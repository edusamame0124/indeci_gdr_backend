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

    /** Three business days for CIE convocation after case reception (RPE 068-2020 Art. 42). */
    public static final int CIE_CONVOCATORIA_BUSINESS_DAYS = 3;

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
        return nthBusinessDayAfter(anchorLastGradeDay, NOTIFY_BUSINESS_DAYS_AFTER_GRADE_END, holidays);
    }

    /**
     * Returns the date of the N-th business day strictly after the anchor date.
     * First counted day = first business day after anchor.
     */
    public static LocalDate nthBusinessDayAfter(LocalDate anchor, int businessDays, Set<LocalDate> holidays) {
        LocalDate d = advanceToBusinessDay(anchor.plusDays(1), holidays);
        for (int i = 1; i < businessDays; i++) {
            d = advanceToBusinessDay(d.plusDays(1), holidays);
        }
        return d;
    }

    /**
     * Counts business days in [fromInclusive, toInclusive]. Returns 0 when the range is empty
     * (fromInclusive after toInclusive). Used for remaining-days countdowns (VAL-04).
     */
    public static int countBusinessDaysInclusive(LocalDate fromInclusive, LocalDate toInclusive, Set<LocalDate> holidays) {
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) {
            return 0;
        }
        int count = 0;
        for (LocalDate d = fromInclusive; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            if (isBusinessDay(d, holidays)) {
                count++;
            }
        }
        return count;
    }

    private static LocalDate advanceToBusinessDay(LocalDate date, Set<LocalDate> holidays) {
        LocalDate d = date;
        while (!isBusinessDay(d, holidays)) {
            d = d.plusDays(1);
        }
        return d;
    }
}
