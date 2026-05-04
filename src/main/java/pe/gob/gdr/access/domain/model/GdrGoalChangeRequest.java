package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "GDR_SOLICITUD_MODIFICACION_META")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrGoalChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrSolicitudModificacionMeta")
    @SequenceGenerator(
            name = "sqGdrSolicitudModificacionMeta",
            sequenceName = "SQ_GDR_SOLICITUD_MOD_META",
            allocationSize = 1
    )
    @Column(name = "ID_SOLICITUD_MODIFICACION_META")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_GOAL", nullable = false)
    private GdrGoal goal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ASSIGNMENT", nullable = false)
    private GdrEvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USUARIO_SOLICITANTE", nullable = false)
    private User requestedByUser;

    @Column(name = "USUARIO_SOLICITANTE", nullable = false, length = 60)
    private String requestedByUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "CODIGO_TIPO_MODIFICACION", nullable = false, length = 40)
    private GoalChangeRequestType requestType;

    @Column(name = "MOTIVO_SOLICITUD", nullable = false, length = 1000)
    private String reason;

    @Column(name = "COMENTARIO_SOLICITUD", length = 1000)
    private String comment;

    @Column(name = "FECHA_REVISION_ORH")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USUARIO_REVISION_ORH")
    private User reviewedByUser;

    @Column(name = "USUARIO_REVISION_ORH", length = 60)
    private String reviewedByUsername;

    @Column(name = "COMENTARIO_REVISION_ORH", length = 1000)
    private String orhReviewComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "CODIGO_ESTADO_SOLICITUD", nullable = false, length = 20)
    @Builder.Default
    private GoalChangeRequestStatus status = GoalChangeRequestStatus.PENDIENTE;

    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 20)
    @Builder.Default
    private String recordStatus = "ACTIVO";

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "USUARIO_ACTUALIZACION", nullable = false, length = 60)
    private String updatedByUsername;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (updatedByUsername == null || updatedByUsername.isBlank()) {
            updatedByUsername = requestedByUsername;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
