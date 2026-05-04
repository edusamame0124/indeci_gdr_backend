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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_RESULTADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrResultado")
    @SequenceGenerator(name = "sqGdrResultado", sequenceName = "SQ_GDR_RESULTADO", allocationSize = 1)
    @Column(name = "ID_RESULT")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ASSIGNMENT", nullable = false)
    private GdrEvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_FINAL_EVALUATION", nullable = false)
    private GdrFinalEvaluation finalEvaluation;

    @Column(name = "CONSOLIDATED_SCORE", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal consolidatedScore = BigDecimal.ZERO;

    @Column(name = "QUALITATIVE_RATING_CODE", length = 40)
    private String qualitativeRatingCode;

    @Column(name = "QUAL_RATING_NOTIFIED", nullable = false, length = 1)
    @Builder.Default
    private String qualRatingNotified = "N";

    @Column(name = "DIRECTIVE_82_COMPLIANCE", nullable = false, length = 1)
    @Builder.Default
    private String directive82Compliance = "N";

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
