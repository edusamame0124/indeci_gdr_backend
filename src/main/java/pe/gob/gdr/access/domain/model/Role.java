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
@Table(name = "SEC_ROLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqSecRole")
    @SequenceGenerator(name = "sqSecRole", sequenceName = "SQ_SEC_ROLE", allocationSize = 1)
    @Column(name = "ID_ROLE")
    private Long id;

    @Column(name = "ROLE_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "ROLE_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
