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
@Table(name = "DOC_PLANTILLA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocPlantilla")
    @SequenceGenerator(name = "sqDocPlantilla", sequenceName = "SQ_DOC_PLANTILLA", allocationSize = 1)
    @Column(name = "ID_PLANTILLA")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TIPO_DOCUMENTO", nullable = false)
    private DocType docType;

    @Column(name = "NOMBRE_PLANTILLA", nullable = false, length = 150)
    private String templateName;

    @Column(name = "DESCRIPCION", length = 300)
    private String description;

    @Column(name = "CLAVE_ARCHIVO", nullable = false, unique = true, length = 260)
    private String fileKey;

    @Column(name = "NOMBRE_ORIGINAL", nullable = false, length = 180)
    private String originalName;

    @Column(name = "MIME_TYPE", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "TAMANIO_BYTES", nullable = false)
    private Long sizeBytes;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVO";

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
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
