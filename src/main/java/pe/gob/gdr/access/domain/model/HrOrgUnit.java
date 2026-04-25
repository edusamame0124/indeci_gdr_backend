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
@Table(name = "HR_ORG_UNIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrOrgUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqHrOrgUnit")
    @SequenceGenerator(name = "sqHrOrgUnit", sequenceName = "SQ_HR_ORG_UNIT", allocationSize = 1)
    @Column(name = "ID_ORG_UNIT")
    private Long id;

    @Column(name = "UNIT_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "UNIT_NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

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
