package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_CYCLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrCycle")
    @SequenceGenerator(name = "sqGdrCycle", sequenceName = "SQ_GDR_CYCLE", allocationSize = 1)
    @Column(name = "ID_CYCLE")
    private Long id;

    @Column(name = "CYCLE_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "CYCLE_NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "FINAL_EVAL_GRADE_START_DATE")
    private LocalDate finalEvalGradeStartDate;

    @Column(name = "FINAL_EVAL_GRADE_END_DATE")
    private LocalDate finalEvalGradeEndDate;

    @Column(name = "QUAL_NOTIFY_DEADLINE_DATE")
    private LocalDate qualNotifyDeadlineDate;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
