package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QualitativeRatingSpreadsheetLabelsTest {

    @Test
    void distinguishesExcelCapitalizationAgainstApiLabelEnum() {
        assertThat(QualitativeRatingSpreadsheetLabels.forQualitativeCode("DISTINGUIDO"))
                .isEqualTo("Rendimiento Distinguido");

        assertThat(QualitativeRatingSpreadsheetLabels.forQualitativeCode("NO_CALIFICABLE"))
                .isEqualTo("No corresponde calificación");
    }
}
