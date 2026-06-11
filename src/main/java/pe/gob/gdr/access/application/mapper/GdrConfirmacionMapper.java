package pe.gob.gdr.access.application.mapper;

import org.springframework.stereotype.Component;
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.policy.QualitativeRating;

@Component
public class GdrConfirmacionMapper {

    public SolicitudConfirmacionResponse toSolicitudResponse(GdrSolicitudConfirmacion solicitud, GdrCasoCie caso) {
        GdrFinalEvaluation evaluation = solicitud.getFinalEvaluation();
        return new SolicitudConfirmacionResponse(
                solicitud.getId(),
                evaluation.getId(),
                solicitud.getEvaluado().getId(),
                solicitud.getEvaluado().getDisplayName(),
                solicitud.getCycle().getName(),
                solicitud.getFechaSolicitud(),
                solicitud.getSustentoEvaluado(),
                solicitud.getEstado(),
                solicitudEstadoLabel(solicitud.getEstado()),
                solicitud.getFechaDerivacionCie(),
                evaluation.getPlazoSolicitudConfirmacion(),
                caso != null ? caso.getNumeroCaso() : null,
                caso != null ? caso.getEstado() : null,
                caso != null ? caso.getDecision() : null,
                caso != null ? caso.getCalificacionResultado() : null,
                caso != null ? QualitativeRating.labelOf(caso.getCalificacionResultado()) : null,
                caso != null ? caso.getSustentoCie() : null,
                caso != null ? caso.getFechaDecision() : null
        );
    }

    public CasoCieResponse toCasoResponse(GdrCasoCie caso, Integer diasHabilesRestantes, boolean convocatoriaVencida) {
        GdrSolicitudConfirmacion solicitud = caso.getSolicitud();
        GdrFinalEvaluation evaluation = solicitud.getFinalEvaluation();
        GdrEvaluationAssignment assignment = evaluation.getAssignment();
        return new CasoCieResponse(
                caso.getId(),
                caso.getNumeroCaso(),
                solicitud.getId(),
                evaluation.getId(),
                solicitud.getEvaluado().getId(),
                solicitud.getEvaluado().getDisplayName(),
                assignment.getEvaluatorPerson().getDisplayName(),
                solicitud.getCycle().getName(),
                caso.getFechaIngresoCie(),
                caso.getPlazoConvocatoria(),
                diasHabilesRestantes,
                convocatoriaVencida,
                caso.getEstado(),
                casoEstadoLabel(caso.getEstado()),
                solicitud.getSustentoEvaluado(),
                evaluation.getConsolidatedScore(),
                evaluation.getQualitativeRatingCode(),
                QualitativeRating.labelOf(evaluation.getQualitativeRatingCode()),
                caso.getDecision(),
                caso.getCalificacionResultado(),
                QualitativeRating.labelOf(caso.getCalificacionResultado()),
                caso.getSustentoCie(),
                caso.getFechaDecision()
        );
    }

    private String solicitudEstadoLabel(String estado) {
        return switch (estado) {
            case GdrSolicitudConfirmacion.ESTADO_PRESENTADA -> "Presentada";
            case GdrSolicitudConfirmacion.ESTADO_EN_CIE     -> "Derivada al CIE";
            case GdrSolicitudConfirmacion.ESTADO_RESUELTA   -> "Resuelta";
            default                                          -> estado;
        };
    }

    private String casoEstadoLabel(String estado) {
        return switch (estado) {
            case GdrCasoCie.ESTADO_RECIBIDO -> "Recibido — pendiente de resolución";
            case GdrCasoCie.ESTADO_RESUELTO -> "Resuelto";
            default                          -> estado;
        };
    }
}
