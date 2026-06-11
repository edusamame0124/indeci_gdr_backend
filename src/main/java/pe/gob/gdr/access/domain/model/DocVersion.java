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
@Table(name = "DOC_VERSION_DOCUMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocVersionDocumento")
    @SequenceGenerator(name = "sqDocVersionDocumento", sequenceName = "SQ_DOC_VERSION_DOCUMENTO", allocationSize = 1)
    @Column(name = "ID_VERSION_DOCUMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_DOCUMENTO_FIRMADO", nullable = false)
    private DocSignedFile signedFile;

    @Column(name = "NUMERO_VERSION", nullable = false)
    private Integer versionNumber;

    @Column(name = "CLAVE_ARCHIVO", nullable = false, length = 260)
    private String fileKey;

    @Column(name = "TAMANIO_BYTES", nullable = false)
    private Long sizeBytes;

    @Column(name = "USUARIO_REGISTRO", nullable = false, length = 120)
    private String registeredUser;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVO";

    /** P6-03 — Referencia normativa vigente al registrar la versión documental. */
    @Column(name = "REF_NORMATIVA", length = 120)
    private String refNormativa;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (registeredAt == null) {
            registeredAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
