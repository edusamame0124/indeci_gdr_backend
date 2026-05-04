package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;

class FormatoGdrExportMappingsTest {

    @Test
    void resolveEvaluatedSegmentFallsBackWhenNoAssignment() {
        assertThat(FormatoGdrExportMappings.resolveEvaluatedSegmentDisplayLiteral(null, Map.of()))
                .isEqualTo(FormatoGdrExportMappings.SELECT_PLACEHOLDER);
    }

    @Test
    void directive82MapsToSpanishBookLiterals() {
        assertThat(FormatoGdrExportMappings.mapDirective82ToBook("Y")).isEqualTo("Sí");
        assertThat(FormatoGdrExportMappings.mapDirective82ToBook("n")).isEqualTo("No");
        assertThat(FormatoGdrExportMappings.mapDirective82ToBook(null)).isEqualTo("[Seleccione]");
    }

    @Test
    void evidenceStatusMapsToSeguimientoLiterals() {
        assertThat(FormatoGdrExportMappings.mapSeguimientoLiteral(null)).isEqualTo("No presenta evidencia");
        GdrEvidence approved = evidenceWithStatus("APPROVED");
        assertThat(FormatoGdrExportMappings.mapSeguimientoLiteral(approved)).isEqualTo("Logrado");
        assertThat(FormatoGdrExportMappings.mapSeguimientoLiteral(evidenceWithStatus("REGISTERED")))
                .isEqualTo("En proceso de logro");
    }

    @Test
    void evidenceStatusMapsToFinalEvidenceLiterals() {
        assertThat(FormatoGdrExportMappings.mapEvidenciaFinalLiteral(null))
                .isEqualTo("NO presenta evidencia de logro final");
        assertThat(FormatoGdrExportMappings.mapEvidenciaFinalLiteral(evidenceWithStatus("APPROVED")))
                .isEqualTo("Sí presenta evidencia final");
        assertThat(FormatoGdrExportMappings.mapEvidenciaFinalLiteral(evidenceWithStatus("REGISTERED")))
                .isEqualTo("NO presenta evidencia de logro final");
    }

    private static GdrEvidence evidenceWithStatus(String code) {
        GdrEvidenceStatus status = new GdrEvidenceStatus();
        status.setStatusCode(code);
        GdrEvidence evidence = new GdrEvidence();
        evidence.setEvidenceStatus(status);
        return evidence;
    }
}
