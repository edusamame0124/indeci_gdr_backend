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
@Table(name = "HR_PERSON")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqHrPerson")
    @SequenceGenerator(name = "sqHrPerson", sequenceName = "SQ_HR_PERSON", allocationSize = 1)
    @Column(name = "ID_PERSON")
    private Long id;

    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "DISPLAY_NAME", nullable = false, length = 150)
    private String displayName;

    /** P6 — Formato GDR 2025 (RPE 000041-2025/PE): denominación del puesto. */
    @Column(name = "CARGO", length = 200)
    private String cargo;

    /** P6 — Formato GDR 2025: nivel remunerativo institucional. */
    @Column(name = "NIVEL_REMUNERATIVO", length = 80)
    private String nivelRemunerativo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ORG_UNIT", nullable = false)
    private HrOrgUnit orgUnit;

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
