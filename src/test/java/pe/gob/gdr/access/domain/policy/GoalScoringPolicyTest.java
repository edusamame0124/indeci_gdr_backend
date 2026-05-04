package pe.gob.gdr.access.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GoalScoringPolicy — fórmula y caps SERVIR (numeral 3.3.1)")
class GoalScoringPolicyTest {

    @Nested
    @DisplayName("Tabla 7 SERVIR — ejemplos oficiales de puntuación por meta")
    class TablaSieteServir {

        @Test
        @DisplayName("Inspecciones sanitarias 93% sobre meta 90% con peso 40% = 41.33")
        void inspeccionesSanitarias() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("90"),
                    new BigDecimal("93"),
                    new BigDecimal("40"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("41.33"), score);
        }

        @Test
        @DisplayName("Promedio inspecciones 12 sobre meta 10 con peso 35% = 42.00")
        void promedioInspecciones() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("10"),
                    new BigDecimal("12"),
                    new BigDecimal("35"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("42.00"), score);
        }

        @Test
        @DisplayName("Capacitaciones 62% sobre meta 70% con peso 25% ≈ 22.14 (entero SERVIR: 22)")
        void capacitaciones() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("70"),
                    new BigDecimal("62"),
                    new BigDecimal("25"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("22.14"), score);
            assertEquals(new BigDecimal("22"), score.setScale(0, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("Suma de las tres puntuaciones ≈ 105.47 (entero SERVIR: 105)")
        void puntuacionFinalEjemploTablaSiete() {
            BigDecimal a = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("90"), new BigDecimal("93"), new BigDecimal("40"), 2, RoundingMode.HALF_UP);
            BigDecimal b = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("10"), new BigDecimal("12"), new BigDecimal("35"), 2, RoundingMode.HALF_UP);
            BigDecimal c = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("70"), new BigDecimal("62"), new BigDecimal("25"), 2, RoundingMode.HALF_UP);

            BigDecimal final_ = GoalScoringPolicy.capFinalScore(
                    a.add(b).add(c), 2, RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("105.47"), final_);
            assertEquals(new BigDecimal("105"), final_.setScale(0, RoundingMode.HALF_UP));
        }
    }

    @Nested
    @DisplayName("Caps SERVIR")
    class Caps {

        @Test
        @DisplayName("Sobre-cumplimiento extremo no excede 120 por meta")
        void capPorMeta() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("10"),
                    new BigDecimal("100"),
                    new BigDecimal("40"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("120.00"), score);
        }

        @Test
        @DisplayName("Suma final superior a 120 se trunca a 120")
        void capFinal() {
            BigDecimal capped = GoalScoringPolicy.capFinalScore(
                    new BigDecimal("180.50"), 2, RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("120.00"), capped);
        }

        @Test
        @DisplayName("Suma final inferior a 120 se respeta")
        void noCapCuandoNoCorresponde() {
            BigDecimal capped = GoalScoringPolicy.capFinalScore(
                    new BigDecimal("105.33"), 2, RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("105.33"), capped);
        }
    }

    @Nested
    @DisplayName("Calificación cualitativa SERVIR (numeral 3.3.1.b)")
    class ClasificacionCualitativa {

        @Test
        @DisplayName("Directivo con 70.00 → BUEN_RENDIMIENTO")
        void directivoUmbralExacto() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("70.00"), "DIRECTIVO");
            assertEquals(QualitativeRating.BUEN_RENDIMIENTO, rating);
        }

        @Test
        @DisplayName("Directivo con 69.99 → SUJETO_OBSERVACION")
        void directivoBajoUmbral() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("69.99"), "DIRECTIVO");
            assertEquals(QualitativeRating.SUJETO_OBSERVACION, rating);
        }

        @Test
        @DisplayName("Otros segmentos con 60.00 → BUEN_RENDIMIENTO")
        void otrosUmbralExacto() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("60.00"), "GENERAL");
            assertEquals(QualitativeRating.BUEN_RENDIMIENTO, rating);
        }

        @Test
        @DisplayName("Otros segmentos con 59.99 → SUJETO_OBSERVACION")
        void otrosBajoUmbral() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("59.99"), "GENERAL");
            assertEquals(QualitativeRating.SUJETO_OBSERVACION, rating);
        }

        @Test
        @DisplayName("Directivo con 65 → SUJETO_OBSERVACION (umbral más exigente)")
        void directivoEntre60Y70() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("65"), "DIRECTIVO");
            assertEquals(QualitativeRating.SUJETO_OBSERVACION, rating);
        }

        @Test
        @DisplayName("Otros segmentos con 65 → BUEN_RENDIMIENTO (umbral más laxo)")
        void otrosEntre60Y70() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("65"), "GENERAL");
            assertEquals(QualitativeRating.BUEN_RENDIMIENTO, rating);
        }

        @Test
        @DisplayName("Score = 0 → SUJETO_OBSERVACION (no NO_CALIFICABLE)")
        void scoreCero() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    BigDecimal.ZERO, "GENERAL");
            assertEquals(QualitativeRating.SUJETO_OBSERVACION, rating);
        }

        @Test
        @DisplayName("Score = null → null (sin clasificación)")
        void scoreNull() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(null, "GENERAL");
            assertNull(rating);
        }

        @Test
        @DisplayName("Segmento desconocido → defaultea al umbral 60")
        void segmentoDesconocido() {
            QualitativeRating rating = GoalScoringPolicy.classifyRating(
                    new BigDecimal("60"), "DESCONOCIDO");
            assertEquals(QualitativeRating.BUEN_RENDIMIENTO, rating);
        }

        @Test
        @DisplayName("Segmento null → defaultea al umbral 60")
        void segmentoNull() {
            QualitativeRating ratingPasa = GoalScoringPolicy.classifyRating(
                    new BigDecimal("60"), null);
            QualitativeRating ratingNoPasa = GoalScoringPolicy.classifyRating(
                    new BigDecimal("59.99"), null);
            assertEquals(QualitativeRating.BUEN_RENDIMIENTO, ratingPasa);
            assertEquals(QualitativeRating.SUJETO_OBSERVACION, ratingNoPasa);
        }

        @Test
        @DisplayName("Etiquetas en español de los códigos cualitativos")
        void etiquetas() {
            assertEquals("Buen rendimiento", QualitativeRating.labelOf("BUEN_RENDIMIENTO"));
            assertEquals("Rendimiento sujeto a observación", QualitativeRating.labelOf("SUJETO_OBSERVACION"));
            assertEquals("Rendimiento distinguido", QualitativeRating.labelOf("DISTINGUIDO"));
            assertEquals("Desaprobado", QualitativeRating.labelOf("DESAPROBADO"));
            assertEquals("No calificable", QualitativeRating.labelOf("NO_CALIFICABLE"));
            assertNull(QualitativeRating.labelOf(null));
            assertNull(QualitativeRating.labelOf("CODIGO_INEXISTENTE"));
        }
    }

    @Nested
    @DisplayName("Casos límite")
    class Bordes {

        @Test
        @DisplayName("Logro = valor meta entrega exactamente el peso")
        void logroIgualMeta() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("70"),
                    new BigDecimal("70"),
                    new BigDecimal("25"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("25.00"), score);
        }

        @Test
        @DisplayName("Logro cero entrega puntaje cero")
        void logroCero() {
            BigDecimal score = GoalScoringPolicy.calculateGoalScore(
                    new BigDecimal("70"),
                    BigDecimal.ZERO,
                    new BigDecimal("25"),
                    2,
                    RoundingMode.HALF_UP);

            assertEquals(new BigDecimal("0.00"), score);
        }
    }
}
