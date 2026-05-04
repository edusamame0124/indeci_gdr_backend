package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_GOAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrGoal")
    @SequenceGenerator(name = "sqGdrGoal", sequenceName = "SQ_GDR_GOAL", allocationSize = 1)
    @Column(name = "ID_GOAL")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ASSIGNMENT", nullable = false)
    private GdrEvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INDICATOR", nullable = false)
    private GdrIndicator indicator;

    @Column(name = "GOAL_TITLE", nullable = false, length = 180)
    private String title;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "EXPECTED_VALUE", nullable = false, precision = 18, scale = 4)
    private BigDecimal expectedValue;

    @Column(name = "WEIGHT", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "ACHIEVED_VALUE", precision = 18, scale = 4)
    private BigDecimal achievedValue;

    @Column(name = "CALCULATED_SCORE", precision = 18, scale = 4)
    private BigDecimal calculatedScore;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate startDate;

    @Column(name = "FECHA_FIN", nullable = false)
    private LocalDate endDate;

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
