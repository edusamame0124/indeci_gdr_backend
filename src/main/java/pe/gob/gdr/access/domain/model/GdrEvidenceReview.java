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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_REVISION_EVIDENCIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvidenceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrRevisionEvidencia")
    @SequenceGenerator(name = "sqGdrRevisionEvidencia", sequenceName = "SQ_GDR_REVISION_EVIDENCIA", allocationSize = 1)
    @Column(name = "ID_EVIDENCE_REVIEW")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVIDENCE", nullable = false)
    private GdrEvidence evidence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVIDENCE_STATUS", nullable = false)
    private GdrEvidenceStatus evidenceStatus;

    @Column(name = "REVIEW_COMMENT", length = 1000)
    private String reviewComment;

    @Column(name = "CODIGO_CALIFICACION_EVIDENCIA", length = 40)
    private String qualificationCode;

    @Column(name = "REVIEWED_AT", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (reviewedAt == null) {
            reviewedAt = now;
        }
        createdAt = now;
    }
}
