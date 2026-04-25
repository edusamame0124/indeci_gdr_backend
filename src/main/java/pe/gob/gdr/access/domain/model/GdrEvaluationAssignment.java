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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_EVALUATION_ASSIGNMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvaluationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEvaluationAssignment")
    @SequenceGenerator(
            name = "sqGdrEvaluationAssignment",
            sequenceName = "SQ_GDR_EVALUATION_ASSIGNMENT",
            allocationSize = 1
    )
    @Column(name = "ID_ASSIGNMENT")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CYCLE", nullable = false)
    private ActiveCycle cycle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVALUATOR_PERSON", nullable = false)
    private HrPerson evaluatorPerson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVALUATED_PERSON", nullable = false)
    private HrPerson evaluatedPerson;

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
