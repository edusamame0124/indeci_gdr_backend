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
@Table(name = "GDR_VALUE_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrValueType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrValueType")
    @SequenceGenerator(name = "sqGdrValueType", sequenceName = "SQ_GDR_VALUE_TYPE", allocationSize = 1)
    @Column(name = "ID_VALUE_TYPE")
    private Long id;

    @Column(name = "VALUE_TYPE_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "VALUE_TYPE_NAME", nullable = false, length = 100)
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
