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
@Table(name = "GDR_OPORTUNIDAD_MEJORA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrImprovementOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrOportunidadMejora")
    @SequenceGenerator(
            name = "sqGdrOportunidadMejora",
            sequenceName = "SQ_GDR_OPORTUNIDAD_MEJORA",
            allocationSize = 1
    )
    @Column(name = "ID_OPORTUNIDAD_MEJORA")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_RESULTADO", nullable = false)
    private GdrResult result;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ESTADO_OPORTUNIDAD_MEJORA", nullable = false)
    private GdrImprovementStatus improvementStatus;

    @Column(name = "DESCRIPCION", nullable = false, length = 1000)
    private String description;

    @Column(name = "RESPONSABLE", nullable = false, length = 180)
    private String responsible;

    @Column(name = "PLAZO_COMPROMISO", nullable = false)
    private LocalDate targetDate;

    @Column(name = "COMENTARIO_CIERRE", length = 1000)
    private String closureComment;

    @Column(name = "USUARIO_REGISTRO", nullable = false, length = 120)
    private String registeredUser;

    @Column(name = "FECHA_CIERRE")
    private LocalDateTime closedAt;

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
