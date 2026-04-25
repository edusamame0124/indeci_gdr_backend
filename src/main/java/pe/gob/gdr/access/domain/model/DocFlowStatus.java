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
@Table(name = "DOC_ESTADO_FLUJO_DOCUMENTAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocFlowStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocEstadoFlujoDocumental")
    @SequenceGenerator(
            name = "sqDocEstadoFlujoDocumental",
            sequenceName = "SQ_DOC_ESTADO_FLUJO_DOCUMENTAL",
            allocationSize = 1
    )
    @Column(name = "ID_ESTADO_FLUJO_DOCUMENTAL")
    private Long id;

    @Column(name = "CODIGO_ESTADO_FLUJO", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "NOMBRE_ESTADO_FLUJO", nullable = false, length = 120)
    private String name;

    @Column(name = "DESCRIPCION", length = 300)
    private String description;

    @Column(name = "ORDEN_FLUJO", nullable = false)
    @Builder.Default
    private Integer flowOrder = 0;

    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 20)
    @Builder.Default
    private String recordStatus = "ACTIVO";

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
