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
@Table(name = "DOC_DOCUMENTO_FIRMADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocSignedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocDocumentoFirmado")
    @SequenceGenerator(name = "sqDocDocumentoFirmado", sequenceName = "SQ_DOC_DOCUMENTO_FIRMADO", allocationSize = 1)
    @Column(name = "ID_DOCUMENTO_FIRMADO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_RESULTADO", nullable = false)
    private GdrResult result;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TIPO_DOCUMENTO", nullable = false)
    private DocType docType;

    @Column(name = "NOMBRE_ORIGINAL", nullable = false, length = 180)
    private String originalName;

    @Column(name = "MIME_TYPE", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "TAMANIO_BYTES", nullable = false)
    private Long sizeBytes;

    @Column(name = "VERSION_ACTUAL", nullable = false)
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "CLAVE_ARCHIVO", nullable = false, unique = true, length = 260)
    private String fileKey;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVO";

    @Column(name = "USUARIO_CARGA", nullable = false, length = 120)
    private String uploadUser;

    @Column(name = "FECHA_CARGA", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (uploadDate == null) {
            uploadDate = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
