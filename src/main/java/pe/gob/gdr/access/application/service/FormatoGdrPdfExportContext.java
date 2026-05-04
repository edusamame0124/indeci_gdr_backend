package pe.gob.gdr.access.application.service;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrResult;

/**
 * Contexto para generar el Formato GDR en PDF: asignación en ciclo activo y, si existe, resultado consolidado.
 */
public record FormatoGdrPdfExportContext(GdrEvaluationAssignment assignment, Optional<GdrResult> consolidatedResult) {

    public boolean isDraft() {
        return consolidatedResult.isEmpty();
    }
}
