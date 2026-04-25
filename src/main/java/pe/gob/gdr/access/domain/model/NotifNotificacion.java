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
@Table(name = "NOTIF_NOTIFICACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqNotifNotificacion")
    @SequenceGenerator(name = "sqNotifNotificacion", sequenceName = "SQ_NOTIF_NOTIFICACION", allocationSize = 1)
    @Column(name = "ID_NOTIFICACION")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USUARIO_DESTINO", nullable = false)
    private User usuarioDestino;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTILLA_NOTIFICACION", nullable = false)
    private NotifPlantilla plantilla;

    @Column(name = "CODIGO_EVENTO", nullable = false, length = 60)
    private String codigoEvento;

    @Column(name = "TITULO_NOTIFICACION", nullable = false, length = 200)
    private String tituloNotificacion;

    @Column(name = "MENSAJE_NOTIFICACION", nullable = false, length = 1000)
    private String mensajeNotificacion;

    @Column(name = "ESTADO_NOTIFICACION", nullable = false, length = 20)
    @Builder.Default
    private String estadoNotificacion = "NO_LEIDA";

    @Column(name = "FECHA_ENVIO", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "FECHA_LECTURA")
    private LocalDateTime fechaLectura;

    @Column(name = "REFERENCIA_NEGOCIO", length = 200)
    private String referenciaNegocio;

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
        if (fechaEnvio == null) {
            fechaEnvio = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
