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

/**
 * Informe de cierre del ciclo GDR.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 55 (plazo hasta 31 de mayo).
 */
@Entity
@Table(name = "GDR_INFORME_CIERRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrInformeCierre {

    public static final String ESTADO_BORRADOR = "BORRADOR";
    public static final String ESTADO_VALIDADO = "VALIDADO";
    public static final String ESTADO_REMITIDO = "REMITIDO";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrInformeCierre")
    @SequenceGenerator(name = "sqGdrInformeCierre", sequenceName = "SQ_GDR_INFORME_CIERRE", allocationSize = 1)
    @Column(name = "ID_INFORME_CIERRE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CICLO", nullable = false)
    private ActiveCycle cycle;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = ESTADO_BORRADOR;

    @Column(name = "TOTAL_EVALUADOS", nullable = false)
    @Builder.Default
    private Integer totalEvaluados = 0;

    @Column(name = "TOTAL_BUEN_RENDIMIENTO", nullable = false)
    @Builder.Default
    private Integer totalBuenRendimiento = 0;

    @Column(name = "TOTAL_SUJETO_OBSERVACION", nullable = false)
    @Builder.Default
    private Integer totalSujetoObservacion = 0;

    @Column(name = "TOTAL_DESAPROBADO", nullable = false)
    @Builder.Default
    private Integer totalDesaprobado = 0;

    @Column(name = "TOTAL_DISTINGUIDO", nullable = false)
    @Builder.Default
    private Integer totalDistinguido = 0;

    @Column(name = "TOTAL_OPORTUNIDADES_MEJORA", nullable = false)
    @Builder.Default
    private Integer totalOportunidadesMejora = 0;

    @Column(name = "TOTAL_CONFIRMACIONES", nullable = false)
    @Builder.Default
    private Integer totalConfirmaciones = 0;

    @Column(name = "TOTAL_CONFIRMACIONES_RESUELTAS", nullable = false)
    @Builder.Default
    private Integer totalConfirmacionesResueltas = 0;

    @Column(name = "TOTAL_DOCUMENTOS_FIRMADOS", nullable = false)
    @Builder.Default
    private Integer totalDocumentosFirmados = 0;

    @Column(name = "OBSERVACIONES_ORH", length = 4000)
    private String observacionesOrh;

    @Column(name = "GENERADO_POR", nullable = false, length = 120)
    private String generadoPor;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (fechaGeneracion == null) {
            fechaGeneracion = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
