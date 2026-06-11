package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
 * Solicitud de confirmación de calificación presentada por el evaluado.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 41 (plazo de 5 días hábiles).
 */
@Entity
@Table(name = "GDR_SOLICITUD_CONFIRMACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrSolicitudConfirmacion {

    public static final String ESTADO_PRESENTADA = "PRESENTADA";
    public static final String ESTADO_EN_CIE = "EN_CIE";
    public static final String ESTADO_RESUELTA = "RESUELTA";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrSolicitudConf")
    @SequenceGenerator(name = "sqGdrSolicitudConf", sequenceName = "SQ_GDR_SOLICITUD_CONF", allocationSize = 1)
    @Column(name = "ID_SOLICITUD")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FINAL_EVALUATION", nullable = false, unique = true)
    private GdrFinalEvaluation finalEvaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVALUADO", nullable = false)
    private HrPerson evaluado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CICLO", nullable = false)
    private ActiveCycle cycle;

    @Column(name = "FECHA_SOLICITUD", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "SUSTENTO_EVALUADO", nullable = false, length = 2000)
    private String sustentoEvaluado;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = ESTADO_PRESENTADA;

    @Column(name = "FECHA_DERIVACION_CIE")
    private LocalDateTime fechaDerivacionCie;

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
