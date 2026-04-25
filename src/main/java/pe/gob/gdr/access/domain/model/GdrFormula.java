package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "GDR_FORMULA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrFormula")
    @SequenceGenerator(name = "sqGdrFormula", sequenceName = "SQ_GDR_FORMULA", allocationSize = 1)
    @Column(name = "ID_FORMULA")
    private Long id;

    @Column(name = "FORMULA_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "FORMULA_NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
