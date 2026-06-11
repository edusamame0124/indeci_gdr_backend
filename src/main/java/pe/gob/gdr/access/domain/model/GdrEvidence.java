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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_EVIDENCIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEvidencia")
    @SequenceGenerator(name = "sqGdrEvidencia", sequenceName = "SQ_GDR_EVIDENCIA", allocationSize = 1)
    @Column(name = "ID_EVIDENCE")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_GOAL", nullable = false)
    private GdrGoal goal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVIDENCE_STATUS", nullable = false)
    private GdrEvidenceStatus evidenceStatus;

    @Column(name = "EVIDENCE_TITLE", nullable = false, length = 180)
    private String title;

    @Column(name = "EVIDENCE_DETAIL", length = 1000)
    private String detail;

    @Column(name = "CODIGO_TIPO_EVIDENCIA", nullable = false, length = 40)
    private String evidenceTypeCode;

    @Column(name = "CODIGO_FORMATO_ESPERADO", nullable = false, length = 40)
    private String expectedFormatCode;

    @Column(name = "EXPECTED_DATE")
    private LocalDate expectedDate;

    @Column(name = "FILE_KEY", length = 500)
    private String fileKey;

    @Column(name = "FILE_ORIGINAL_NAME", length = 255)
    private String fileOriginalName;

    @Column(name = "FILE_MIME_TYPE", length = 120)
    private String fileMimeType;

    @Column(name = "FILE_SIZE_BYTES")
    private Long fileSizeBytes;

    @Column(name = "FILE_UPLOADED_AT")
    private LocalDateTime fileUploadedAt;

    @Column(name = "IS_MANDATORY", nullable = false)
    @Builder.Default
    private boolean mandatory = false;

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
