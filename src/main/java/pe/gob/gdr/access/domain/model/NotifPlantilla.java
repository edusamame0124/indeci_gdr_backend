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
@Table(name = "NOTIF_PLANTILLA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifPlantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqNotifPlantilla")
    @SequenceGenerator(name = "sqNotifPlantilla", sequenceName = "SQ_NOTIF_PLANTILLA", allocationSize = 1)
    @Column(name = "ID_PLANTILLA_NOTIFICACION")
    private Long id;

    @Column(name = "CODIGO_PLANTILLA", nullable = false, length = 60)
    private String codigoPlantilla;

    @Column(name = "NOMBRE_PLANTILLA", nullable = false, length = 160)
    private String nombrePlantilla;

    @Column(name = "ASUNTO", nullable = false, length = 200)
    private String asunto;

    @Column(name = "CUERPO_MENSAJE", nullable = false, length = 1000)
    private String cuerpoMensaje;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CANAL", nullable = false)
    private NotifCanal canal;

    @Column(name = "VERSION_PLANTILLA", nullable = false)
    @Builder.Default
    private Integer versionPlantilla = 1;

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
