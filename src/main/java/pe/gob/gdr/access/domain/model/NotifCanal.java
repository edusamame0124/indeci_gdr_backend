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
@Table(name = "NOTIF_CANAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifCanal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqNotifCanal")
    @SequenceGenerator(name = "sqNotifCanal", sequenceName = "SQ_NOTIF_CANAL", allocationSize = 1)
    @Column(name = "ID_CANAL")
    private Long id;

    @Column(name = "CODIGO_CANAL", nullable = false, length = 40)
    private String codigoCanal;

    @Column(name = "NOMBRE_CANAL", nullable = false, length = 120)
    private String nombreCanal;

    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;

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
