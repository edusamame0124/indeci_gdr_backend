package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateParticipantStatusRequest(
    @NotNull(message = "El estado es obligatorio.") String status
) {}
