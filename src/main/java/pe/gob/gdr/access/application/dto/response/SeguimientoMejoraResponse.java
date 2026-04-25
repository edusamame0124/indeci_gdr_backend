package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record SeguimientoMejoraResponse(
        Long idSeguimientoOportunidadMejora,
        String comentarioSeguimiento,
        String usuarioRegistro,
        LocalDateTime fechaRegistro
) {
}
