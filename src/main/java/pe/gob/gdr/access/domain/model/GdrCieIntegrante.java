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
 * Integrante del CIE dentro de una conformación.
 * Roles: TITULAR_ORH, REP_EVALUADOS_SEG, DESIGNADO_CASO, ACCESITARIO.
 * El integrante puede ser un usuario del sistema (idPersona) o externo (nombreExterno).
 * Referencia: RPE 068-2020-SERVIR-PE Art. 42-44.
 */
@Entity
@Table(name = "GDR_CIE_INTEGRANTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrCieIntegrante {

    public static final String ROL_TITULAR_ORH       = "TITULAR_ORH";
    public static final String ROL_REP_EVALUADOS_SEG = "REP_EVALUADOS_SEG";
    public static final String ROL_DESIGNADO_CASO    = "DESIGNADO_CASO";
    public static final String ROL_ACCESITARIO       = "ACCESITARIO";

    public static final String ESTADO_ACTIVO   = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrCieIntegrante")
    @SequenceGenerator(name = "sqGdrCieIntegrante", sequenceName = "SQ_GDR_CIE_INTEGRANTE", allocationSize = 1)
    @Column(name = "ID_INTEGRANTE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONFORMACION", nullable = false)
    private GdrCieConformacion conformacion;

    @Column(name = "ROL_CIE", nullable = false, length = 50)
    private String rolCie;

    @Column(name = "SEGMENTO", length = 100)
    private String segmento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PERSONA")
    private HrPerson persona;

    @Column(name = "NOMBRE_EXTERNO", length = 150)
    private String nombreExterno;

    @Column(name = "CARGO_DESCRIPCION", length = 200)
    private String cargoDescripcion;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN")
    private LocalDate fechaFin;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = ESTADO_ACTIVO;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String resolveNombreDisplay() {
        if (persona != null) {
            return persona.getDisplayName();
        }
        return nombreExterno != null ? nombreExterno : "(sin nombre)";
    }
}
