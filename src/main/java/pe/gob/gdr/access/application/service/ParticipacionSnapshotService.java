package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.AjustarSnapshotRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;

/**
 * Gestiona el snapshot de cargo/puesto del evaluado en la asignación GDR.
 *
 * Fuente oficial en dev: HR_PERSON.CARGO + HR_PERSON.NIVEL_REMUNERATIVO + HrOrgUnit.name.
 * Ajuste manual solo como excepción auditada: motivo obligatorio + snapshot del valor anterior.
 *
 * P1 (comentado): integrarse con SISRH API para obtener puesto laboral,
 *   unidad orgánica y régimen del legajo del empleado.
 *   FUENTE_DATO_SNAP = 'SISRH_API' cuando se active.
 *
 * POSIBLE_CAMBIO_RRHH_GDR_004.
 */
@Service
public class ParticipacionSnapshotService {

    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final AuditTrailService auditTrailService;

    public ParticipacionSnapshotService(
            GdrEvaluationAssignmentRepository assignmentRepository,
            AuditTrailService auditTrailService) {
        this.assignmentRepository = assignmentRepository;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Captura el snapshot inicial de cargo/puesto para una asignación recién creada.
     * Llamar desde el servicio de asignaciones al crear GdrEvaluationAssignment.
     */
    @Transactional
    public void capturarSnapshot(GdrEvaluationAssignment assignment) {
        var evaluado = assignment.getEvaluatedPerson();
        var evaluador = assignment.getEvaluatorPerson();

        assignment.setCargoPuestoSnap(evaluado.getCargo());
        assignment.setNivelRemunerativoSnap(evaluado.getNivelRemunerativo());
        assignment.setUnidadOrganicaSnap(
                evaluado.getOrgUnit() != null ? evaluado.getOrgUnit().getName() : null);
        assignment.setEvaluadorSnap(evaluador.getDisplayName());
        assignment.setFechaCorteSnap(LocalDate.now());
        assignment.setFuenteDatoSnap("GDR_HRPERSON");
        assignment.setDatoAjustadoManualmente("N");

        // P1: llamar a SisrhLegajoClient.obtenerPuestoVigente(evaluado.getDocumentNumber())
        //   y poblar desde la respuesta. Cambiar fuenteDatoSnap a "SISRH_API".
    }

    /**
     * Ajuste manual de cargo/puesto: excepción auditada.
     * Requiere motivo, guarda el valor anterior como evidencia de cambio.
     */
    @Transactional
    public void ajustarManualmente(Long assignmentId, AjustarSnapshotRequest request, String username) {
        if (request.motivoAjuste() == null || request.motivoAjuste().isBlank()) {
            throw new DomainException(
                    "El motivo del ajuste manual es obligatorio. "
                    + "El ajuste de cargo/puesto es una excepción auditada.");
        }

        GdrEvaluationAssignment assignment = assignmentRepository.findByIdForAdministration(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación no encontrada: " + assignmentId));

        String valorAnterior = assignment.getCargoPuestoSnap();
        assignment.setCargoPuestoAnterior(valorAnterior);
        assignment.setCargoPuestoSnap(request.nuevoCargoPuesto().trim());
        assignment.setDatoAjustadoManualmente("S");
        assignment.setFuenteDatoSnap("AJUSTE_MANUAL");
        assignment.setMotivoAjusteManual(request.motivoAjuste().trim());
        assignment.setAjustadoPor(username);

        assignmentRepository.save(assignment);

        auditTrailService.recordEvent(
                "SNAPSHOT_CARGO_AJUSTADO",
                username,
                String.format(
                        "Ajuste manual de cargo en asignación %d: [%s] → [%s]. Motivo: %s",
                        assignmentId, valorAnterior, request.nuevoCargoPuesto(), request.motivoAjuste()),
                null
        );
    }
}
