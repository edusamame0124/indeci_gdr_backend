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
@Table(name = "GDR_AUDIT_EVENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqGdrAuditEvent")
    @SequenceGenerator(name = "sqGdrAuditEvent", sequenceName = "SQ_GDR_AUDIT_EVENT", allocationSize = 1)
    @Column(name = "ID_AUDIT_EVENT")
    private Long id;

    @Column(name = "EVENT_CODE", nullable = false, length = 60)
    private String eventCode;

    @Column(name = "PRINCIPAL", length = 120)
    private String principal;

    @Column(name = "DETAIL", length = 500)
    private String detail;

    @Column(name = "CLIENT_IP", length = 64)
    private String clientIp;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "REQUEST_PATH", length = 250)
    private String requestPath;

    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    void onCreate() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
