package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAssignmentSegmentRequest(
        @NotNull(message = "El segmento es obligatorio.")
        Long segmentId
) {
}
