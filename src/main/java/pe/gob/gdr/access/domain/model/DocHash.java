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
@Table(name = "DOC_HASH_DOCUMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocHash {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocHashDocumento")
    @SequenceGenerator(name = "sqDocHashDocumento", sequenceName = "SQ_DOC_HASH_DOCUMENTO", allocationSize = 1)
    @Column(name = "ID_HASH_DOCUMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_DOCUMENTO_FIRMADO", nullable = false)
    private DocSignedFile signedFile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_VERSION_DOCUMENTO", nullable = false)
    private DocVersion documentVersion;

    @Column(name = "ALGORITMO_HASH", nullable = false, length = 30)
    @Builder.Default
    private String hashAlgorithm = "SHA-256";

    @Column(name = "VALOR_HASH", nullable = false, length = 128)
    private String hashValue;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
