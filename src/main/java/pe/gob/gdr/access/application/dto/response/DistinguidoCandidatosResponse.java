package pe.gob.gdr.access.application.dto.response;

import java.util.List;

public record DistinguidoCandidatosResponse(
        int notifiedUniverseTotal,
        int maxDistinguidosSlots,
        int currentDistinguidosAssigned,
        int remainingDistinguidoSlots,
        int candidatosConConfirmacionPendiente,
        boolean bloqueoVal08Activo,
        List<DistinguidoCandidatoFilaResponse> rows
) {
}
