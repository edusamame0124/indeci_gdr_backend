package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AjustarSnapshotRequest(

        @NotBlank(message = "El nuevo cargo/puesto es obligatorio.")
        @Size(max = 200, message = "El cargo/puesto no puede superar 200 caracteres.")
        String nuevoCargoPuesto,

        @NotBlank(message = "El motivo del ajuste es obligatorio (excepción auditada).")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.")
        String motivoAjuste
) {}
