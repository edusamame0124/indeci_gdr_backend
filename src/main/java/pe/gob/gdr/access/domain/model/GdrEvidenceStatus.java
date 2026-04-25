package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "GDR_ESTADO_EVIDENCIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvidenceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEstadoEvidencia")
    @SequenceGenerator(name = "sqGdrEstadoEvidencia", sequenceName = "SQ_GDR_ESTADO_EVIDENCIA", allocationSize = 1)
    @Column(name = "ID_EVIDENCE_STATUS")
    private Long id;

    @Column(name = "STATUS_CODE", nullable = false, unique = true, length = 40)
    private String statusCode;

    @Column(name = "STATUS_NAME", nullable = false, length = 100)
    private String statusName;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
