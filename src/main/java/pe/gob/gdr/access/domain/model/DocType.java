package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "DOC_TIPO_DOCUMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocTipoDocumento")
    @SequenceGenerator(name = "sqDocTipoDocumento", sequenceName = "SQ_DOC_TIPO_DOCUMENTO", allocationSize = 1)
    @Column(name = "ID_TIPO_DOCUMENTO")
    private Long id;

    @Column(name = "CODIGO_TIPO_DOCUMENTO", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "NOMBRE_TIPO_DOCUMENTO", nullable = false, length = 120)
    private String name;

    @Column(name = "DESCRIPCION", length = 300)
    private String description;

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
