package pe.gob.gdr.access.application.mapper;

import org.springframework.stereotype.Component;
import pe.gob.gdr.access.application.dto.response.GdrSeguimientoResponse;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;

@Component
public class GdrSeguimientoMapper {

    public GdrSeguimientoResponse toResponse(GdrSeguimiento s) {
        return new GdrSeguimientoResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getCycle().getId(),
                s.getTipoReunion(),
                tipoLabel(s.getTipoReunion()),
                s.getFechaReunion(),
                s.getDescripcionAvance(),
                s.getCompromisos(),
                s.getEstado(),
                s.getEvaluadorId(),
                s.getEvaluadoId(),
                s.getConsentimientoEvaluado() != null && s.getConsentimientoEvaluado() == 1,
                s.getCreatedAt()
        );
    }

    private String tipoLabel(String tipo) {
        return switch (tipo) {
            case "SEGUIMIENTO_PERIODICO"        -> "Seguimiento periódico";
            case "RETROALIMENTACION_PERIODICA"  -> "Retroalimentación periódica";
            default                             -> tipo;
        };
    }
}
