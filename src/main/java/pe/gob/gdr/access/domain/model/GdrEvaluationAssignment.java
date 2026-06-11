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
@Table(name = "GDR_EVALUATION_ASSIGNMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvaluationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEvaluationAssignment")
    @SequenceGenerator(
            name = "sqGdrEvaluationAssignment",
            sequenceName = "SQ_GDR_EVALUATION_ASSIGNMENT",
            allocationSize = 1
    )
    @Column(name = "ID_ASSIGNMENT")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CYCLE", nullable = false)
    private ActiveCycle cycle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVALUATOR_PERSON", nullable = false)
    private HrPerson evaluatorPerson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_EVALUATED_PERSON", nullable = false)
    private HrPerson evaluatedPerson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SEGMENT", nullable = false)
    private GdrSegment segment;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /** V95 — Snapshot de cargo/puesto al inicio del ciclo. POSIBLE_CAMBIO_RRHH_GDR_004. */
    @Column(name = "CARGO_PUESTO_SNAP", length = 200)
    private String cargoPuestoSnap;

    @Column(name = "NIVEL_REMUNERATIVO_SNAP", length = 80)
    private String nivelRemunerativoSnap;

    @Column(name = "UNIDAD_ORGANICA_SNAP", length = 200)
    private String unidadOrganicaSnap;

    @Column(name = "EVALUADOR_SNAP", length = 150)
    private String evaluadorSnap;

    @Column(name = "FECHA_CORTE_SNAP")
    private java.time.LocalDate fechaCorteSnap;

    @Column(name = "FUENTE_DATO_SNAP", length = 50)
    @Builder.Default
    private String fuenteDatoSnap = "GDR_HRPERSON";

    @Column(name = "DATO_AJUSTADO_MANUALMENTE", nullable = false, length = 1)
    @Builder.Default
    private String datoAjustadoManualmente = "N";

    @Column(name = "MOTIVO_AJUSTE_MANUAL", length = 500)
    private String motivoAjusteManual;

    @Column(name = "CARGO_PUESTO_ANTERIOR", length = 200)
    private String cargoPuestoAnterior;

    @Column(name = "AJUSTADO_POR", length = 120)
    private String ajustadoPor;
    // P1: fuenteDatoSnap = 'SISRH_API' — integración con legajo SISRH en tiempo real

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
