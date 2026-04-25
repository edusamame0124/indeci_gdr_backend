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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_USER_CONTEXT_ASSIGNMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContextAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrUserContextAssignment")
    @SequenceGenerator(
            name = "sqGdrUserContextAssignment",
            sequenceName = "SQ_GDR_USER_CONTEXT_ASSIGNMENT",
            allocationSize = 1
    )
    @Column(name = "ID_USER_CONTEXT_ASSIGNMENT")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CYCLE", nullable = false)
    private ActiveCycle cycle;

    @Column(name = "CONTEXT_CODE", length = 60)
    private String contextCode;

    @Column(name = "CONTEXT_NAME", length = 180)
    private String contextName;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "ASSIGNED_AT", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
