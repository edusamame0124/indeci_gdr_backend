package pe.gob.gdr.access.application.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.RegistrarCieConformacionRequest;
import pe.gob.gdr.access.application.dto.response.CieConformacionResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCieConformacion;
import pe.gob.gdr.access.domain.model.GdrCieIntegrante;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCieConformacionRepository;

/**
 * Gestiona la conformación del CIE.
 * POSIBLE_CAMBIO_RRHH_GDR_001: configurable por ciclo o institucional.
 * Referencia: RPE 068-2020-SERVIR-PE Art. 42-48.
 */
@Service
public class GdrCieConformacionService {

    private static final Map<String, String> ROL_LABELS = Map.of(
            GdrCieIntegrante.ROL_TITULAR_ORH,       "Jefe de ORH / Representante",
            GdrCieIntegrante.ROL_REP_EVALUADOS_SEG, "Representante de evaluados por segmento",
            GdrCieIntegrante.ROL_DESIGNADO_CASO,    "Designado según tipo de caso",
            GdrCieIntegrante.ROL_ACCESITARIO,       "Integrante accesitario"
    );

    private final GdrCieConformacionRepository conformacionRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final AuditTrailService auditTrailService;

    public GdrCieConformacionService(
            GdrCieConformacionRepository conformacionRepository,
            ActiveCycleRepository activeCycleRepository,
            AuditTrailService auditTrailService) {
        this.conformacionRepository = conformacionRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public List<CieConformacionResponse> listar() {
        return conformacionRepository.findAllOrderByVigenciaInicioDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CieConformacionResponse obtener(Long id) {
        return toResponse(loadConformacion(id));
    }

    @Transactional
    public CieConformacionResponse registrar(RegistrarCieConformacionRequest request, String username) {
        validarFechas(request.vigenciaInicio(), request.vigenciaFin());
        validarIntegrantes(request.integrantes());

        ActiveCycle cycle = resolveCycle(request.cycleId());

        GdrCieConformacion conformacion = GdrCieConformacion.builder()
                .cycle(cycle)
                .resolucionNumero(trimOrNull(request.resolucionNumero()))
                .resolucionFecha(request.resolucionFecha())
                .vigenciaInicio(request.vigenciaInicio())
                .vigenciaFin(request.vigenciaFin())
                .observaciones(trimOrNull(request.observaciones()))
                .registradoPor(username)
                .build();

        List<GdrCieIntegrante> integrantes = request.integrantes().stream()
                .map(dto -> buildIntegrante(dto, conformacion))
                .toList();
        conformacion.setIntegrantes(integrantes);

        GdrCieConformacion saved = conformacionRepository.save(conformacion);

        auditTrailService.recordEvent(
                "CIE_CONFORMACION_REGISTRADA",
                username,
                "Conformación CIE registrada (id=" + saved.getId() + ", ciclo=" +
                        (cycle != null ? cycle.getCode() : "institucional") + ").",
                null
        );

        return toResponse(saved);
    }

    @Transactional
    public CieConformacionResponse anular(Long id, String username) {
        GdrCieConformacion conformacion = loadConformacion(id);
        if (GdrCieConformacion.ESTADO_ANULADO.equals(conformacion.getEstado())) {
            throw new DomainException("La conformación ya se encuentra anulada.");
        }
        conformacion.setEstado(GdrCieConformacion.ESTADO_ANULADO);
        GdrCieConformacion saved = conformacionRepository.save(conformacion);
        auditTrailService.recordEvent(
                "CIE_CONFORMACION_ANULADA",
                username,
                "Conformación CIE anulada (id=" + id + ").",
                null
        );
        return toResponse(saved);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void validarFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (fin != null && fin.isBefore(inicio)) {
            throw new DomainException("La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
        }
    }

    private void validarIntegrantes(List<RegistrarCieConformacionRequest.IntegranteRequest> integrantes) {
        boolean tieneOrh = integrantes.stream()
                .anyMatch(i -> GdrCieIntegrante.ROL_TITULAR_ORH.equals(i.rolCie()));
        if (!tieneOrh) {
            throw new DomainException(
                    "El CIE debe incluir al menos un integrante con rol TITULAR_ORH (Jefe de ORH o representante). "
                    + "Referencia: RPE 068-2020-SERVIR-PE Art. 42.");
        }
        for (var dto : integrantes) {
            if (dto.idPersona() == null && (dto.nombreExterno() == null || dto.nombreExterno().isBlank())) {
                throw new DomainException(
                        "Cada integrante debe tener asignado un usuario del sistema (idPersona) "
                        + "o un nombre externo (nombreExterno).");
            }
        }
    }

    private ActiveCycle resolveCycle(Long cycleId) {
        if (cycleId == null) {
            return null;
        }
        return activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado: " + cycleId));
    }

    private GdrCieIntegrante buildIntegrante(
            RegistrarCieConformacionRequest.IntegranteRequest dto,
            GdrCieConformacion conformacion) {

        HrPerson persona = null;
        if (dto.idPersona() != null) {
            persona = new HrPerson();
            persona.setId(dto.idPersona());
        }

        return GdrCieIntegrante.builder()
                .conformacion(conformacion)
                .rolCie(dto.rolCie())
                .segmento(trimOrNull(dto.segmento()))
                .persona(persona)
                .nombreExterno(trimOrNull(dto.nombreExterno()))
                .cargoDescripcion(trimOrNull(dto.cargoDescripcion()))
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .build();
    }

    private GdrCieConformacion loadConformacion(Long id) {
        return conformacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conformación CIE no encontrada: " + id));
    }

    private CieConformacionResponse toResponse(GdrCieConformacion c) {
        List<CieConformacionResponse.IntegranteDto> integrantesDto = c.getIntegrantes().stream()
                .map(this::toIntegranteDto).toList();
        return new CieConformacionResponse(
                c.getId(),
                c.getCycle() != null ? c.getCycle().getId() : null,
                c.getCycle() != null ? c.getCycle().getName() : "Institucional",
                c.getResolucionNumero(),
                c.getResolucionFecha(),
                c.getVigenciaInicio(),
                c.getVigenciaFin(),
                c.getObservaciones(),
                c.getEstado(),
                estadoLabel(c.getEstado()),
                c.getRegistradoPor(),
                c.getCreatedAt(),
                integrantesDto
        );
    }

    private CieConformacionResponse.IntegranteDto toIntegranteDto(GdrCieIntegrante i) {
        return new CieConformacionResponse.IntegranteDto(
                i.getId(),
                i.getRolCie(),
                ROL_LABELS.getOrDefault(i.getRolCie(), i.getRolCie()),
                i.getSegmento(),
                i.getPersona() != null ? i.getPersona().getId() : null,
                i.resolveNombreDisplay(),
                i.getCargoDescripcion(),
                i.getFechaInicio(),
                i.getFechaFin(),
                i.getEstado()
        );
    }

    private String estadoLabel(String estado) {
        return switch (estado) {
            case "VIGENTE"  -> "Vigente";
            case "VENCIDO"  -> "Vencido";
            case "ANULADO"  -> "Anulado";
            default         -> estado;
        };
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
