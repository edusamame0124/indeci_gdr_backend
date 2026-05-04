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
@Table(name = "GDR_ENVIO_META_ORH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrGoalOrhSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEnvioMetaOrh")
    @SequenceGenerator(
            name = "sqGdrEnvioMetaOrh",
            sequenceName = "SQ_GDR_ENVIO_META_ORH",
            allocationSize = 1
    )
    @Column(name = "ID_ENVIO_META_ORH")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_GOAL", nullable = false)
    private GdrGoal goal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ASSIGNMENT", nullable = false)
    private GdrEvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USUARIO_ENVIO", nullable = false)
    private User submittedByUser;

    @Column(name = "USUARIO_ENVIO", nullable = false, length = 60)
    private String submittedByUsername;

    @Column(name = "ACTOR_FUNCIONAL_ENVIO", nullable = false, length = 40)
    private String submittedFunctionalActor;

    @Column(name = "COMENTARIO_ENVIO", length = 1000)
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
    @Column(name = "CODIGO_ESTADO_ENVIO", nullable = false, length = 20)
    @Builder.Default
    private GoalOrhSubmissionStatus status = GoalOrhSubmissionStatus.ENVIADO;

    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 20)
    @Builder.Default
    private String recordStatus = "ACTIVO";

    @Column(name = "FECHA_ENVIO", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "USUARIO_ACTUALIZACION", nullable = false, length = 60)
    private String updatedByUsername;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        submittedAt = now;
        createdAt = now;
        updatedAt = now;
        if (updatedByUsername == null || updatedByUsername.isBlank()) {
            updatedByUsername = submittedByUsername;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
