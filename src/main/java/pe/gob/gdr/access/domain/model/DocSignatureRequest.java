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
@Table(name = "DOC_SOLICITUD_FIRMA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocSignatureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqDocSolicitudFirma")
    @SequenceGenerator(name = "sqDocSolicitudFirma", sequenceName = "SQ_DOC_SOLICITUD_FIRMA", allocationSize = 1)
    @Column(name = "ID_SOLICITUD_FIRMA")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_RESULTADO", nullable = false)
    private GdrResult result;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TIPO_DOCUMENTO", nullable = false)
    private DocType docType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTILLA", nullable = false)
    private DocTemplate template;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ESTADO_FLUJO_DOCUMENTAL", nullable = false)
    private DocFlowStatus flowStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_DOCUMENTO_FIRMADO")
    private DocSignedFile signedDocument;

    @Column(name = "PROVEEDOR_FIRMA", nullable = false, length = 40)
    private String signatureProvider;

    @Column(name = "ID_TRANSACCION_EXTERNA", length = 120)
    private String externalTransactionId;

    @Column(name = "CLAVE_ARCHIVO_PREPARADO", nullable = false, length = 260)
    private String preparedFileKey;

    @Column(name = "NOMBRE_ARCHIVO_PREPARADO", nullable = false, length = 180)
    private String preparedOriginalName;

    @Column(name = "MIME_TYPE_PREPARADO", nullable = false, length = 120)
    private String preparedMimeType;

    @Column(name = "HASH_ARCHIVO_PREPARADO", nullable = false, length = 128)
    private String preparedHash;

    @Column(name = "USUARIO_SOLICITUD", length = 120)
    private String requestUser;

    @Column(name = "FECHA_SOLICITUD")
    private LocalDateTime requestDate;

    @Column(name = "FECHA_INICIO_FIRMA")
    private LocalDateTime signatureStartDate;

    @Column(name = "FECHA_RETORNO")
    private LocalDateTime returnDate;

    @Column(name = "CODIGO_RESULTADO_FIRMA", length = 40)
    private String signatureResultCode;

    @Column(name = "MENSAJE_RESULTADO_FIRMA", length = 300)
    private String signatureResultMessage;

    @Column(name = "IND_FLUJO_ACTIVO", nullable = false, length = 1)
    @Builder.Default
    private String activeFlowIndicator = "S";

    @Column(name = "FECHA_REGISTRO_DOCUMENTO")
    private LocalDateTime documentRegisteredAt;

    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 20)
    @Builder.Default
    private String recordStatus = "ACTIVO";

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
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
