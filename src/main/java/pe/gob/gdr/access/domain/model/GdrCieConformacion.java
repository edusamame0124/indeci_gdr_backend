package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Conformación del Comité Institucional de Evaluación (CIE).
 * Configurable por ciclo o de alcance institucional (ID_CICLO nulo).
 * Referencia: RPE 068-2020-SERVIR-PE Art. 42-48.
 * POSIBLE_CAMBIO_RRHH_GDR_001.
 */
@Entity
@Table(name = "GDR_CIE_CONFORMACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrCieConformacion {

    public static final String ESTADO_VIGENTE = "VIGENTE";
    public static final String ESTADO_VENCIDO = "VENCIDO";
    public static final String ESTADO_ANULADO = "ANULADO";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrCieConformacion")
    @SequenceGenerator(name = "sqGdrCieConformacion", sequenceName = "SQ_GDR_CIE_CONFORMACION", allocationSize = 1)
    @Column(name = "ID_CONFORMACION")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CICLO")
    private ActiveCycle cycle;

    @Column(name = "RESOLUCION_NUMERO", length = 100)
    private String resolucionNumero;

    @Column(name = "RESOLUCION_FECHA")
    private LocalDate resolucionFecha;

    @Column(name = "VIGENCIA_INICIO", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "VIGENCIA_FIN")
    private LocalDate vigenciaFin;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = ESTADO_VIGENTE;

    @Column(name = "REGISTRADO_POR", nullable = false, length = 120)
    private String registradoPor;

    @OneToMany(mappedBy = "conformacion", fetch = FetchType.LAZY)
    @Builder.Default
    private List<GdrCieIntegrante> integrantes = new ArrayList<>();

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
