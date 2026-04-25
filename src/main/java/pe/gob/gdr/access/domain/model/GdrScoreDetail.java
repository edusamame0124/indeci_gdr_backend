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
@Table(name = "GDR_DETALLE_PUNTAJE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrScoreDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrDetallePuntaje")
    @SequenceGenerator(name = "sqGdrDetallePuntaje", sequenceName = "SQ_GDR_DETALLE_PUNTAJE", allocationSize = 1)
    @Column(name = "ID_SCORE_DETAIL")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_FINAL_EVALUATION", nullable = false)
    private GdrFinalEvaluation finalEvaluation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_GOAL", nullable = false)
    private GdrGoal goal;

    @Column(name = "ACHIEVED_VALUE", nullable = false, precision = 18, scale = 4)
    private BigDecimal achievedValue;

    @Column(name = "SCORE_VALUE", nullable = false, precision = 18, scale = 4)
    private BigDecimal scoreValue;

    @Column(name = "DETAIL_COMMENT", length = 500)
    private String detailComment;

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
