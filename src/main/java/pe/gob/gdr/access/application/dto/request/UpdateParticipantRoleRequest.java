package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateParticipantRoleRequest(
    @NotNull(message = "El rol es obligatorio.") String role
) {}
