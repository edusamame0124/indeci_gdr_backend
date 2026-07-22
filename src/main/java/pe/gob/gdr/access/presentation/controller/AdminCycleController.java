package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CreateCycleRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CicloAvanceChecklistResponse;
import pe.gob.gdr.access.application.dto.response.CicloBoardContextResponse;
import pe.gob.gdr.access.application.dto.response.CicloConCronogramaResponse;
import pe.gob.gdr.access.application.dto.response.CycleAccessResponse;
import pe.gob.gdr.access.application.dto.response.CycleOptionResponse;
import pe.gob.gdr.access.application.dto.response.PlanningChecklistResponse;
import pe.gob.gdr.access.application.service.AdminAssignmentService;
import pe.gob.gdr.access.application.service.GdrCicloBoardContextService;
import pe.gob.gdr.access.application.service.GdrCicloEstadoService;
import pe.gob.gdr.access.application.service.GdrCronogramaService;

@RestController
@RequestMapping("/admin/cycles")
public class AdminCycleController {

    private final AdminAssignmentService adminAssignmentService;
    private final GdrCicloEstadoService cicloEstadoService;
    private final GdrCronogramaService cronogramaService;
    private final GdrCicloBoardContextService boardContextService;

    public AdminCycleController(
            AdminAssignmentService adminAssignmentService,
            GdrCicloEstadoService cicloEstadoService,
            GdrCronogramaService cronogramaService,
            GdrCicloBoardContextService boardContextService) {
        this.adminAssignmentService = adminAssignmentService;
        this.cicloEstadoService = cicloEstadoService;
        this.cronogramaService = cronogramaService;
        this.boardContextService = boardContextService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canListAdminCycles(authentication)")
    public ResponseEntity<ApiResponse<List<CycleOptionResponse>>> listCycles() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.listCycles(),
                "Ciclos consultados correctamente."
        ));
    }

    /**
     * Crea un nuevo ciclo GDR en estado BORRADOR.
     * Solo ROLE_GDR_ORH puede ejecutarlo. Valida que no exista ciclo en curso.
     */
    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<CycleOptionResponse>> createCycle(
            @RequestBody @Valid CreateCycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                cicloEstadoService.createCycle(request),
                "Ciclo GDR creado correctamente en estado Borrador."));
    }

    /**
     * Valida que el ciclo existe y retorna su estado más el flag isActiveCycle.
     * Usado por requireCycleGuard en frontend para determinar si el ciclo navegado
     * es el activo del backend y controlar módulos backend-singleton.
     */
    @GetMapping("/{cycleId}/validate-access")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<CycleAccessResponse>> validateAccess(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cicloEstadoService.validateCycleAccess(cycleId),
                "Acceso al ciclo validado."));
    }

    /** Detalle del ciclo con estado de etapa y cronograma — ORH y ADMIN. */
    @GetMapping("/{cycleId}/detalle")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<CicloConCronogramaResponse>> getCicloDetalle(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.getCicloCronograma(cycleId),
                "Detalle del ciclo consultado correctamente."));
    }

    /**
     * Avanza el ciclo al siguiente estado normativo.
     * Solo ORH puede ejecutarlo. Valida restricciones normativas.
     */
    @PostMapping("/{cycleId}/avanzar-etapa")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<CicloConCronogramaResponse>> avanzarEtapa(
            @PathVariable Long cycleId) {
        cicloEstadoService.avanzarEtapa(cycleId);
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.getCicloCronograma(cycleId),
                "Etapa del ciclo avanzada correctamente."));
    }

    /**
     * Checklist de requisitos para la SIGUIENTE transición de etapa (la que
     * avanzar-etapa aplicaría), en modo consulta. Permite mostrar en pantalla,
     * en cualquier etapa del ciclo, qué falta antes de avanzar.
     */
    @GetMapping("/{cycleId}/checklist-avance")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<CicloAvanceChecklistResponse>> getChecklistAvance(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                cicloEstadoService.previsualizarChecklistAvance(cycleId),
                "Checklist de avance consultado correctamente."));
    }

    /**
     * Anula el ciclo — solo ADMIN_SISTEMA.
     */
    @PostMapping("/{cycleId}/anular")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<CicloConCronogramaResponse>> anularCiclo(
            @PathVariable Long cycleId) {
        cicloEstadoService.anular(cycleId);
        return ResponseEntity.ok(ApiResponse.ok(
                cronogramaService.getCicloCronograma(cycleId),
                "Ciclo anulado."));
    }

    /**
     * Contexto del tablero GDR: flags de acceso, estado y resumen del checklist.
     * Consumido por el frontend para computar el estado visual de cada tarjeta.
     */
    @GetMapping("/{cycleId}/board-context")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<CicloBoardContextResponse>> getBoardContext(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                boardContextService.getBoardContext(cycleId),
                "Contexto del tablero consultado correctamente."));
    }

    /**
     * Checklist detallado de planificación (13 campos) para el bloque de cumplimiento.
     * ORH y ADMIN pueden consultarlo.
     */
    @GetMapping("/{cycleId}/planning-checklist")
    @PreAuthorize("@gdrAccessPolicyService.canViewCronograma(authentication)")
    public ResponseEntity<ApiResponse<PlanningChecklistResponse>> getPlanningChecklist(
            @PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                boardContextService.getPlanningChecklist(cycleId),
                "Checklist de planificación consultado correctamente."));
    }
}
