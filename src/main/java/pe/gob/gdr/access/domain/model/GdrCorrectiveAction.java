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
@Table(name = "GDR_ACCION_CORRECTIVA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrCorrectiveAction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrAccionCorrectiva")
    @SequenceGenerator(name = "sqGdrAccionCorrectiva", sequenceName = "SQ_GDR_ACCION_CORRECTIVA", allocationSize = 1)
    @Column(name = "ID_CORRECTIVE_ACTION")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVIDENCE_REVIEW", nullable = false)
    private GdrEvidenceReview evidenceReview;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVIDENCE", nullable = false)
    private GdrEvidence evidence;

    @Column(name = "ACTION_DETAIL", nullable = false, length = 1000)
    private String actionDetail;

    @Column(name = "ACTION_STATUS", nullable = false, length = 20)
    @Builder.Default
    private String actionStatus = "OPEN";

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

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
