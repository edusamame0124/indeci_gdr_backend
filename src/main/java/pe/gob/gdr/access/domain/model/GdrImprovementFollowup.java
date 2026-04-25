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
@Table(name = "GDR_SEGUIMIENTO_OPORTUNIDAD_MEJORA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrImprovementFollowup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrSeguimientoOportunidadMejora")
    @SequenceGenerator(
            name = "sqGdrSeguimientoOportunidadMejora",
            sequenceName = "SQ_GDR_SEGUIMIENTO_OPORTUNIDAD_MEJORA",
            allocationSize = 1
    )
    @Column(name = "ID_SEGUIMIENTO_OPORTUNIDAD_MEJORA")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_OPORTUNIDAD_MEJORA", nullable = false)
    private GdrImprovementOpportunity opportunity;

    @Column(name = "COMENTARIO_SEGUIMIENTO", nullable = false, length = 1000)
    private String followupComment;

    @Column(name = "USUARIO_REGISTRO", nullable = false, length = 120)
    private String registeredUser;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (registeredAt == null) {
            registeredAt = now;
        }
    }
}
