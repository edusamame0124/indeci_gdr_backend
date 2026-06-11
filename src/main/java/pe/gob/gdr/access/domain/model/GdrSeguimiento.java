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
@Table(name = "GDR_SEGUIMIENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrSeguimiento")
    @SequenceGenerator(name = "sqGdrSeguimiento", sequenceName = "SQ_GDR_SEGUIMIENTO", allocationSize = 1)
    @Column(name = "ID_SEGUIMIENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ASSIGNMENT", nullable = false)
    private GdrEvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CICLO", nullable = false)
    private ActiveCycle cycle;

    @Column(name = "TIPO_REUNION", nullable = false, length = 50)
    @Builder.Default
    private String tipoReunion = "SEGUIMIENTO_PERIODICO";

    @Column(name = "FECHA_REUNION", nullable = false)
    private LocalDate fechaReunion;

    @Column(name = "DESCRIPCION_AVANCE", length = 2000)
    private String descripcionAvance;

    @Column(name = "COMPROMISOS", length = 2000)
    private String compromisos;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = "REALIZADA";

    @Column(name = "ID_EVALUADOR")
    private Long evaluadorId;

    @Column(name = "ID_EVALUADO")
    private Long evaluadoId;

    @Column(name = "CONSENTIMIENTO_EVALUADO", nullable = false)
    @Builder.Default
    private Integer consentimientoEvaluado = 0;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
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
