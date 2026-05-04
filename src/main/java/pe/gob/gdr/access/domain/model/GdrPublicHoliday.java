package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GDR_PUBLIC_HOLIDAY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdrPublicHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrPublicHoliday")
    @SequenceGenerator(name = "sqGdrPublicHoliday", sequenceName = "SQ_GDR_PUBLIC_HOLIDAY", allocationSize = 1)
    @Column(name = "ID_PUBLIC_HOLIDAY")
    private Long id;

    @Column(name = "HOLIDAY_DATE", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "DESCRIPTION", length = 200)
    private String description;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
