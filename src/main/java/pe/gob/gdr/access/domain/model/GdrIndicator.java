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
@Table(name = "GDR_INDICATOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrIndicator")
    @SequenceGenerator(name = "sqGdrIndicator", sequenceName = "SQ_GDR_INDICATOR", allocationSize = 1)
    @Column(name = "ID_INDICATOR")
    private Long id;

    @Column(name = "INDICATOR_CODE", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "INDICATOR_NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_VALUE_TYPE", nullable = false)
    private GdrValueType valueType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_FORMULA", nullable = false)
    private GdrFormula formula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SEGMENT", nullable = false)
    private GdrSegment segment;

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
