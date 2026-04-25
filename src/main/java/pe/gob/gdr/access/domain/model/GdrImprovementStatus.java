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
@Table(name = "GDR_ESTADO_OPORTUNIDAD_MEJORA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrImprovementStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEstadoOportunidadMejora")
    @SequenceGenerator(
            name = "sqGdrEstadoOportunidadMejora",
            sequenceName = "SQ_GDR_ESTADO_OPORTUNIDAD_MEJORA",
            allocationSize = 1
    )
    @Column(name = "ID_ESTADO_OPORTUNIDAD_MEJORA")
    private Long id;

    @Column(name = "CODIGO_ESTADO", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "NOMBRE_ESTADO", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPCION", length = 250)
    private String description;

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
