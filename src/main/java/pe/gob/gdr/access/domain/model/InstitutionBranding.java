package pe.gob.gdr.access.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "SEC_INSTITUTION_BRANDING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionBranding {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqSecInstitutionBranding")
    @SequenceGenerator(
            name = "sqSecInstitutionBranding",
            sequenceName = "SQ_SEC_INSTITUTION_BRANDING",
            allocationSize = 1
    )
    @Column(name = "ID_BRANDING")
    private Long id;

    @Column(name = "INSTITUTION_NAME", nullable = false, length = 200)
    private String institutionName;

    @Lob
    @Column(name = "MAIN_LOGO")
    private byte[] mainLogo;

    @Column(name = "MAIN_LOGO_MIME_TYPE", length = 100)
    private String mainLogoMimeType;

    @Lob
    @Column(name = "HEADER_LOGO")
    private byte[] headerLogo;

    @Column(name = "HEADER_LOGO_MIME_TYPE", length = 100)
    private String headerLogoMimeType;

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
