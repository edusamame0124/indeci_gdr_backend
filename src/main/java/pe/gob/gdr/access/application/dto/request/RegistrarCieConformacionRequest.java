package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record RegistrarCieConformacionRequest(

        Long cycleId,

        String resolucionNumero,

        LocalDate resolucionFecha,

        @NotNull(message = "La fecha de inicio de vigencia es obligatoria.")
        LocalDate vigenciaInicio,

        LocalDate vigenciaFin,

        String observaciones,

        @NotEmpty(message = "Debe registrar al menos un integrante del CIE.")
        @Valid
        List<IntegranteRequest> integrantes
) {
    public record IntegranteRequest(

            @NotNull(message = "El rol del integrante es obligatorio.")
            String rolCie,

            String segmento,

            Long idPersona,

            String nombreExterno,

            String cargoDescripcion,

            @NotNull(message = "La fecha de inicio del integrante es obligatoria.")
            LocalDate fechaInicio,

            LocalDate fechaFin
    ) {}
}
