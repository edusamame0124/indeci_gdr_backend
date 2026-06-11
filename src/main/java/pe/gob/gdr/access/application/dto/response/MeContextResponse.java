package pe.gob.gdr.access.application.dto.response;

import java.util.List;

public record MeContextResponse(
        Long cycleId,
        List<String> rolesTecnicos,
        String actorFuncional,
        boolean esEvaluado,
        boolean esEvaluador,
        boolean esMixto,
        Long personId,
        String personDisplayName,
        List<Long> evaluadosAsignados,
        List<String> contextos
) {}
