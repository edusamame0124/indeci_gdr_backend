package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActualizarRequisitosDistinguidoRequest(
        @NotNull Boolean qualRatingNotified,
        @NotNull Boolean directive82ComplianceConfirmed
) {
}
