package pe.gob.gdr.access.application.dto.response;

public record AsignarDistinguidoResultResponse(
        int assignedCount,
        int remainingDistinguidoSlotsAfter
) {
}
