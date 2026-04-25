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
@Table(name = "SEC_USER_ROLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqSecUserRole")
    @SequenceGenerator(name = "sqSecUserRole", sequenceName = "SQ_SEC_USER_ROLE", allocationSize = 1)
    @Column(name = "ID_USER_ROLE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    private Role role;

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
