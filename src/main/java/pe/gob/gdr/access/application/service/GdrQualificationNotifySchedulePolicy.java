package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.calendar.PeruBusinessDayCalendar;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;

@Service
public class GdrQualificationNotifySchedulePolicy {

    static final ZoneId ZONE_LIMA = ZoneId.of("America/Lima");
    private static final int HOLIDAY_LOAD_MARGIN_DAYS = 40;

    private final GdrPublicHolidayRepository publicHolidayRepository;

    public GdrQualificationNotifySchedulePolicy(GdrPublicHolidayRepository publicHolidayRepository) {
        this.publicHolidayRepository = publicHolidayRepository;
    }

    public void assertMailNotifyAllowedToday(ActiveCycle cycle) {
        LocalDate today = LocalDate.now(ZONE_LIMA);
        LocalDate gradeStart = cycle.getFinalEvalGradeStartDate();
        if (gradeStart != null && today.isBefore(gradeStart)) {
            throw new DomainException(
                    "Aún no inicia la ventana de calificación final institucional para notificar por correo "
                            + "(inicio " + gradeStart + ")."
            );
        }
        Optional<LocalDate> deadline = resolveNotifyDeadline(cycle);
        if (deadline.isPresent() && today.isAfter(deadline.get())) {
            throw new DomainException(
                    "Plazo vencido para notificar la calificación por correo en este ciclo "
                            + "(límite " + deadline.get() + ")."
            );
        }
    }

    /**
     * Explicit {@link ActiveCycle#getQualNotifyDeadlineDate()} wins; otherwise five Peru business days after
     * {@link ActiveCycle#getFinalEvalGradeEndDate()} using {@link GdrPublicHolidayRepository}.
     * If neither deadline nor grade end is set, empty (no automatic enforcement — legacy / MVP per ciclo).
     */
    public Optional<LocalDate> resolveNotifyDeadline(ActiveCycle cycle) {
        if (cycle.getQualNotifyDeadlineDate() != null) {
            return Optional.of(cycle.getQualNotifyDeadlineDate());
        }
        if (cycle.getFinalEvalGradeEndDate() == null) {
            return Optional.empty();
        }
        LocalDate gradeEnd = cycle.getFinalEvalGradeEndDate();
        LocalDate from = gradeEnd.plusDays(1);
        LocalDate to = gradeEnd.plusDays(HOLIDAY_LOAD_MARGIN_DAYS);
        Set<LocalDate> holidays = publicHolidayRepository.findHolidayDatesBetween(from, to);
        return Optional.of(PeruBusinessDayCalendar.fifthBusinessDayAfterGradeEnd(gradeEnd, holidays));
    }
}
