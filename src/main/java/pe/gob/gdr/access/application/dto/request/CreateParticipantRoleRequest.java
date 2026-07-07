package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateParticipantRoleRequest(
    @NotNull(message = "El ID del ciclo es obligatorio.") Long cycleId,
    @NotNull(message = "El ID de la persona es obligatorio.") Long personId,
    @NotNull(message = "El rol es obligatorio.") String role
) {}