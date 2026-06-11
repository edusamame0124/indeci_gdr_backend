package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

/**
 * Caso derivado al Comité Institucional de Evaluación (CIE).
 * Normativa: RPE 068-2020-SERVIR-PE Art. 42 (convocatoria en 3 días hábiles;
 * el CIE confirma o modifica la calificación; su decisión es definitiva).
 */
@Entity
@Table(name = "GDR_CASO_CIE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrCasoCie {

    public static final String ESTADO_RECIBIDO = "RECIBIDO";
    public static final String ESTADO_RESUELTO = "RESUELTO";
    public static final String DECISION_CONFIRMA = "CONFIRMA";
    public static final String DECISION_MODIFICA = "MODIFICA";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrCasoCie")
    @SequenceGenerator(name = "sqGdrCasoCie", sequenceName = "SQ_GDR_CASO_CIE", allocationSize = 1)
    @Column(name = "ID_CASO_CIE")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SOLICITUD", nullable = false, unique = true)
    private GdrSolicitudConfirmacion solicitud;

    @Column(name = "NUMERO_CASO", nullable = false, length = 30, unique = true)
    private String numeroCaso;

    @Column(name = "FECHA_INGRESO_CIE", nullable = false)
    private LocalDateTime fechaIngresoCie;

    /** Fecha límite de convocatoria: +3 días hábiles desde la recepción (alerta, no bloqueo). */
    @Column(name = "PLAZO_CONVOCATORIA")
    private LocalDate plazoConvocatoria;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = ESTADO_RECIBIDO;

    @Column(name = "DECISION", length = 20)
    private String decision;

    @Column(name = "CALIFICACION_RESULTADO", length = 40)
    private String calificacionResultado;

    @Column(name = "SUSTENTO_CIE", length = 2000)
    private String sustentoCie;

    @Column(name = "FECHA_DECISION")
    private LocalDateTime fechaDecision;

    /** Acta de sesión CIE firmada (se integra en P6). */
    @Column(name = "ID_ACTA_DOC")
    private Long actaDocId;

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
