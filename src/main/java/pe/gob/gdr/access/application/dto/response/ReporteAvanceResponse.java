package pe.gob.gdr.access.application.dto.response;

public record ReporteAvanceResponse(
        Long idAsignacion,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        int totalMetas,
        int totalEvidencias,
        int metasConEvidencia,
        boolean evaluacionFinalDisponible,
        boolean resultadoDisponible,
        int totalDocumentosFirmados,
        int oportunidadesAbiertas,
        int oportunidadesCerradas
) {
}
