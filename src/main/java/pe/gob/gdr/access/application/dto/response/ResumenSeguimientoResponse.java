package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ResumenSeguimientoResponse(
        Long assignmentId,
        Long cycleId,
        int totalReuniones,
        LocalDate fechaPrimeraReunion,
        LocalDate fechaUltimaReunion,
        long diasSeguimiento,
        boolean cumpleMinimo6Meses,
        String alertaVAL01,
        List<GdrSeguimientoResponse> reuniones
) {}
