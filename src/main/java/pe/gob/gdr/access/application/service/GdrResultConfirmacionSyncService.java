package pe.gob.gdr.access.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

/**
 * Sincroniza {@code GDR_RESULTADO.ESTADO_CONFIRMACION} con el flujo P4/P5.
 * Trazabilidad VAL-08 (RPE 068-2020 Art. 50).
 */
@Service
public class GdrResultConfirmacionSyncService {

    private final GdrResultRepository resultRepository;

    public GdrResultConfirmacionSyncService(GdrResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Transactional
    public void marcarPendiente(GdrFinalEvaluation evaluation) {
        actualizarEstado(evaluation, GdrResult.ESTADO_CONF_PENDIENTE);
    }

    @Transactional
    public void marcarResuelta(GdrFinalEvaluation evaluation) {
        actualizarEstado(evaluation, GdrResult.ESTADO_CONF_RESUELTA);
    }

    private void actualizarEstado(GdrFinalEvaluation evaluation, String estado) {
        if (evaluation == null || evaluation.getAssignment() == null) {
            return;
        }
        resultRepository.findByAssignmentIdInActiveCycle(evaluation.getAssignment().getId())
                .ifPresent(result -> persistirEstado(result, estado));
    }

    private void persistirEstado(GdrResult result, String estado) {
        result.setEstadoConfirmacion(estado);
        resultRepository.save(result);
    }
}
