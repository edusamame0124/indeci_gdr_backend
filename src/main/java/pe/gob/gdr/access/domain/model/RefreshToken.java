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
@Table(name = "SEC_REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqSecRefreshToken")
    @SequenceGenerator(name = "sqSecRefreshToken", sequenceName = "SQ_SEC_REFRESH_TOKEN", allocationSize = 1)
    @Column(name = "ID_REFRESH_TOKEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @Column(name = "TOKEN_VALUE", nullable = false, unique = true, length = 1000)
    private String tokenValue;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "REVOKED_AT")
    private LocalDateTime revokedAt;

    @Column(name = "CLIENT_IP", length = 64)
    private String clientIp;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
