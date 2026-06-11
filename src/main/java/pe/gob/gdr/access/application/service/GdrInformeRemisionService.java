package pe.gob.gdr.access.application.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.RegistrarRemisionRequest;
import pe.gob.gdr.access.application.dto.response.RemisionResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrInformeCierre;
import pe.gob.gdr.access.domain.model.GdrInformeCierreRemision;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRemisionRepository;
import pe.gob.gdr.access.domain.repository.GdrInformeCierreRepository;

/**
 * Gestiona el registro de evidencia de remisión del informe de cierre a SERVIR.
 *
 * P0: El sistema genera el informe; la remisión a SERVIR es manual con registro de evidencia.
 *   Se registra: fecha, canal, N° trámite, nombre del documento adjunto como cargo/constancia.
 *
 * P1 (comentado): flujo interno de aprobación previo a la remisión.
 *   Requiere roles adicionales (titular ORH, planificación).
 *
 * P2 (comentado): integración automática si SERVIR publica API/portal.
 *   Activar solo cuando SERVIR formalice el mecanismo electrónico.
 *
 * Normativa: RPE 068-2020-SERVIR-PE Art. 55.
 * POSIBLE_CAMBIO_RRHH_GDR_008.
 */
@Service
public class GdrInformeRemisionService {

    private static final Map<String, String> CANAL_LABELS = Map.of(
            GdrInformeCierreRemision.CANAL_MESA_PARTES, "Mesa de partes",
            GdrInformeCierreRemision.CANAL_CORREO,      "Correo electrónico",
            GdrInformeCierreRemision.CANAL_FISICO,      "Entrega física",
            GdrInformeCierreRemision.CANAL_PLATAFORMA,  "Plataforma SERVIR",
            GdrInformeCierreRemision.CANAL_OTRO,        "Otro canal"
    );

    private static final Map<String, String> TIPO_DOC_LABELS = Map.of(
            GdrInformeCierreRemision.TIPO_DOC_CARGO,      "Cargo de recepción",
            GdrInformeCierreRemision.TIPO_DOC_CONSTANCIA, "Constancia de envío",
            GdrInformeCierreRemision.TIPO_DOC_CORREO,     "Correo de respuesta/acuse",
            GdrInformeCierreRemision.TIPO_DOC_OTRO,       "Otro documento"
    );

    private final GdrInformeCierreRepository informeRepository;
    private final GdrInformeCierreRemisionRepository remisionRepository;
    private final AuditTrailService auditTrailService;

    public GdrInformeRemisionService(
            GdrInformeCierreRepository informeRepository,
            GdrInformeCierreRemisionRepository remisionRepository,
            AuditTrailService auditTrailService) {
        this.informeRepository = informeRepository;
        this.remisionRepository = remisionRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public List<RemisionResponse> listar(Long informeId) {
        loadInforme(informeId);
        return remisionRepository.findByInformeCierreIdOrderByFechaRemisionDesc(informeId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RemisionResponse registrar(Long informeId, RegistrarRemisionRequest request, String username) {
        GdrInformeCierre informe = loadInforme(informeId);
        validarCanalRemision(request.canalRemision());

        if (GdrInformeCierre.ESTADO_BORRADOR.equals(informe.getEstado())) {
            throw new DomainException(
                    "Solo se puede registrar la remisión de un informe en estado VALIDADO o REMITIDO. "
                    + "Genere y valide el informe antes de registrar la remisión.");
        }

        GdrInformeCierreRemision remision = GdrInformeCierreRemision.builder()
                .informeCierre(informe)
                .fechaRemision(request.fechaRemision())
                .canalRemision(request.canalRemision())
                .numeroTramite(trimOrNull(request.numeroTramite()))
                .observaciones(trimOrNull(request.observaciones()))
                .nombreDocEvidencia(trimOrNull(request.nombreDocEvidencia()))
                .tipoDocEvidencia(request.tipoDocEvidencia())
                .registradoPor(username)
                .build();

        GdrInformeCierreRemision saved = remisionRepository.save(remision);

        if (!GdrInformeCierre.ESTADO_REMITIDO.equals(informe.getEstado())) {
            informe.setEstado(GdrInformeCierre.ESTADO_REMITIDO);
            informeRepository.save(informe);
        }

        auditTrailService.recordEvent(
                "INFORME_CIERRE_REMITIDO",
                username,
                String.format(
                        "Remisión registrada para informe %d — canal: %s, fecha: %s, trámite: %s.",
                        informeId, request.canalRemision(), request.fechaRemision(),
                        request.numeroTramite() != null ? request.numeroTramite() : "s/n"),
                null
        );

        return toResponse(saved);

        // P1: registrar flujo de aprobación interna antes de marcar como REMITIDO
        // P2: llamar a Servir.remitirInforme(informe, evidencia) y guardar ID respuesta
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void validarCanalRemision(String canal) {
        List<String> canalesValidos = List.of(
                GdrInformeCierreRemision.CANAL_MESA_PARTES,
                GdrInformeCierreRemision.CANAL_CORREO,
                GdrInformeCierreRemision.CANAL_FISICO,
                GdrInformeCierreRemision.CANAL_PLATAFORMA,
                GdrInformeCierreRemision.CANAL_OTRO
        );
        if (!canalesValidos.contains(canal)) {
            throw new DomainException(
                    "Canal de remisión inválido: '" + canal + "'. "
                    + "Valores aceptados: " + String.join(", ", canalesValidos));
        }
    }

    private GdrInformeCierre loadInforme(Long informeId) {
        return informeRepository.findById(informeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Informe de cierre no encontrado: " + informeId));
    }

    private RemisionResponse toResponse(GdrInformeCierreRemision r) {
        return new RemisionResponse(
                r.getId(),
                r.getInformeCierre().getId(),
                r.getFechaRemision(),
                r.getCanalRemision(),
                CANAL_LABELS.getOrDefault(r.getCanalRemision(), r.getCanalRemision()),
                r.getNumeroTramite(),
                r.getObservaciones(),
                r.getNombreDocEvidencia(),
                r.getTipoDocEvidencia(),
                r.getTipoDocEvidencia() != null
                        ? TIPO_DOC_LABELS.getOrDefault(r.getTipoDocEvidencia(), r.getTipoDocEvidencia())
                        : null,
                r.getEstadoRemision(),
                estadoLabel(r.getEstadoRemision()),
                r.getRegistradoPor(),
                r.getCreatedAt()
        );
    }

    private String estadoLabel(String estado) {
        return switch (estado) {
            case "REGISTRADO"  -> "Registrado";
            case "CONFIRMADO"  -> "Confirmado";
            case "OBSERVADO"   -> "Observado";
            default            -> estado;
        };
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
