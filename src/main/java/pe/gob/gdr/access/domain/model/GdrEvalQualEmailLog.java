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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_EVAL_QUAL_EMAIL_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrEvalQualEmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrEvalQualEmailLog")
    @SequenceGenerator(
            name = "sqGdrEvalQualEmailLog",
            sequenceName = "SQ_GDR_EVAL_QUAL_EMAIL_LOG",
            allocationSize = 1)
    @Column(name = "ID_EVAL_QUAL_EMAIL_LOG")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FINAL_EVALUATION", nullable = false)
    private GdrFinalEvaluation finalEvaluation;

    @Column(name = "FECHA_ENVIO", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "DESTINATARIO", nullable = false, length = 254)
    private String recipientEmail;

    @Column(name = "CODIGO_PLANTILLA", length = 40)
    private String templateCode;

    @Column(name = "ASUNTO", length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EvalQualEmailLogStatus status;

    @Column(name = "MENSAJE_ERROR", length = 1000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO_ACCION", nullable = false)
    private User actionUser;

    @PrePersist
    void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
