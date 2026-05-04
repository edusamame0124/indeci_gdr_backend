package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateAssignmentRequest(
        @NotNull(message = "El ciclo es obligatorio.")
        Long cycleId,
        @NotNull(message = "La persona evaluadora es obligatoria.")
        Long evaluatorPersonId,
        @NotNull(message = "La persona evaluada es obligatoria.")
        Long evaluatedPersonId,
        Long segmentId
) {
}
