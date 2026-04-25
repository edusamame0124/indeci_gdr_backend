package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "CONSENT_TIPO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqConsentTipo")
    @SequenceGenerator(name = "sqConsentTipo", sequenceName = "SQ_CONSENT_TIPO", allocationSize = 1)
    @Column(name = "ID_TIPO_CONSENTIMIENTO")
    private Long id;

    @Column(name = "CODIGO_CONSENTIMIENTO", nullable = false, length = 60)
    private String codigoConsentimiento;

    @Column(name = "NOMBRE_CONSENTIMIENTO", nullable = false, length = 180)
    private String nombreConsentimiento;

    @Column(name = "TEXTO_CONSENTIMIENTO", nullable = false, length = 2000)
    private String textoConsentimiento;

    @Column(name = "VERSION_CONSENTIMIENTO", nullable = false)
    @Builder.Default
    private Integer versionConsentimiento = 1;

    @Column(name = "REQUERIDO", nullable = false, length = 1)
    @Builder.Default
    private String requerido = "N";

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
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
