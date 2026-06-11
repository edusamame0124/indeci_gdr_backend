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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evidencia de remisión del informe de cierre GDR a SERVIR.
 * P0: remisión manual controlada con registro de evidencia.
 * P1 (comentado): aprobación interna previa a la remisión.
 * P2 (comentado): integración con API/portal SERVIR.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 55.
 * POSIBLE_CAMBIO_RRHH_GDR_008.
 */
@Entity
@Table(name = "GDR_INFORME_CIERRE_REMISION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrInformeCierreRemision {

    public static final String CANAL_MESA_PARTES      = "MESA_PARTES";
    public static final String CANAL_CORREO           = "CORREO_ELECTRONICO";
    public static final String CANAL_FISICO           = "FISICO";
    public static final String CANAL_PLATAFORMA       = "PLATAFORMA_SERVIR";
    public static final String CANAL_OTRO             = "OTRO";

    public static final String TIPO_DOC_CARGO         = "CARGO_RECEPCION";
    public static final String TIPO_DOC_CONSTANCIA    = "CONSTANCIA_ENVIO";
    public static final String TIPO_DOC_CORREO        = "CORREO_RESPUESTA";
    public static final String TIPO_DOC_OTRO          = "OTRO";

    public static final String ESTADO_REGISTRADO  = "REGISTRADO";
    public static final String ESTADO_CONFIRMADO  = "CONFIRMADO";
    public static final String ESTADO_OBSERVADO   = "OBSERVADO";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrInformeRemision")
    @SequenceGenerator(name = "sqGdrInformeRemision", sequenceName = "SQ_GDR_INFORME_REMISION", allocationSize = 1)
    @Column(name = "ID_REMISION")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INFORME_CIERRE", nullable = false)
    private GdrInformeCierre informeCierre;

    @Column(name = "FECHA_REMISION", nullable = false)
    private LocalDate fechaRemision;

    @Column(name = "CANAL_REMISION", nullable = false, length = 80)
    private String canalRemision;

    @Column(name = "NUMERO_TRAMITE", length = 80)
    private String numeroTramite;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

    @Column(name = "NOMBRE_DOC_EVIDENCIA", length = 300)
    private String nombreDocEvidencia;

    @Column(name = "TIPO_DOC_EVIDENCIA", length = 80)
    private String tipoDocEvidencia;

    @Column(name = "ESTADO_REMISION", nullable = false, length = 30)
    @Builder.Default
    private String estadoRemision = ESTADO_REGISTRADO;

    @Column(name = "REGISTRADO_POR", nullable = false, length = 120)
    private String registradoPor;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // P1: String idAprobador — usuario que aprueba el envío
    // P1: LocalDate fechaAprobacion
    // P1: String estadoAprobacion
    // P2: String idRespuestaServir — token/confirmación de SERVIR API
    // P2: LocalDate fechaConfirmacionServir

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
