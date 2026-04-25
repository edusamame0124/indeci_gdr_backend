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
@Table(name = "CONSENT_REGISTRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqConsentRegistro")
    @SequenceGenerator(name = "sqConsentRegistro", sequenceName = "SQ_CONSENT_REGISTRO", allocationSize = 1)
    @Column(name = "ID_REGISTRO_CONSENTIMIENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TIPO_CONSENTIMIENTO", nullable = false)
    private ConsentTipo tipoConsentimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private User usuario;

    @Column(name = "VERSION_CONSENTIMIENTO", nullable = false)
    private Integer versionConsentimiento;

    @Column(name = "IND_ACEPTADO", nullable = false, length = 1)
    @Builder.Default
    private String indAceptado = "S";

    @Column(name = "FECHA_ACEPTACION", nullable = false)
    private LocalDateTime fechaAceptacion;

    @Column(name = "IP_ACEPTACION", length = 64)
    private String ipAceptacion;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "DETALLE_ACEPTACION", length = 1000)
    private String detalleAceptacion;

    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 20)
    @Builder.Default
    private String estadoRegistro = "ACTIVO";

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (fechaAceptacion == null) {
            fechaAceptacion = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
