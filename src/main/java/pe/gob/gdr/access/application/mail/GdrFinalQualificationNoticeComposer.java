package pe.gob.gdr.access.application.mail;

import java.math.BigDecimal;
import org.springframework.util.StringUtils;
import pe.gob.gdr.access.domain.policy.QualitativeRating;

/** Plantilla de cuerpo y asunto para notificación de calificación final por correo (texto + HTML mínimo). */
public final class GdrFinalQualificationNoticeComposer {

    public static final String TEMPLATE_CODE = "GDR_FINAL_QUAL_EMAIL";

    public record ComposedMail(String subject, String textBody, String htmlBody) {}

    private GdrFinalQualificationNoticeComposer() {}

    public static ComposedMail compose(
            String cycleName,
            String evaluatedDisplayName,
            String qualitativeRatingCode,
            BigDecimal consolidatedScore
    ) {
        String ratingLabel = QualitativeRating.labelOf(qualitativeRatingCode);
        if (!StringUtils.hasText(ratingLabel)) {
            ratingLabel = StringUtils.hasText(qualitativeRatingCode) ? qualitativeRatingCode : "Sin registrar";
        }
        String cycle = StringUtils.hasText(cycleName) ? cycleName : "ciclo GDR activo";
        String person = StringUtils.hasText(evaluatedDisplayName) ? evaluatedDisplayName : "Servidor(a)";

        String scoreText = formatScoreSentence(consolidatedScore);
        String subject = "GDR — Notificación de calificación final";
        String textBody = buildPlainText(cycle, person, ratingLabel, scoreText);
        String htmlBody = buildHtml(cycle, person, ratingLabel, scoreText);
        return new ComposedMail(subject, textBody, htmlBody);
    }

    private static String formatScoreSentence(BigDecimal score) {
        if (score == null) {
            return "";
        }
        return "Puntaje consolidado: " + score.stripTrailingZeros().toPlainString() + ".";
    }

    private static String buildPlainText(String cycle, String person, String ratingLabel, String scoreSentence) {
        StringBuilder sb = new StringBuilder();
        sb.append("Estimado(a) ").append(person).append(",\n\n");
        sb.append("Mediante el sistema GDR le informamos el resultado de su evaluación final correspondiente al ");
        sb.append(cycle).append(".\n\n");
        sb.append("Calificación cualitativa: ").append(ratingLabel).append(".\n");
        if (StringUtils.hasText(scoreSentence)) {
            sb.append(scoreSentence).append("\n");
        }
        sb.append("\nAtentamente,\nGestión del Desempeño (GDR)");
        return sb.toString();
    }

    private static String buildHtml(String cycle, String person, String ratingLabel, String scoreSentence) {
        String scoreBlock = StringUtils.hasText(scoreSentence)
                ? "<p><strong>" + esc(scoreSentence) + "</strong></p>"
                : "";
        return "<p>Estimado(a) " + esc(person) + ",</p>"
                + "<p>Mediante el sistema GDR le informamos el resultado de su evaluación final correspondiente al <strong>"
                + esc(cycle) + "</strong>.</p>"
                + "<p><strong>Calificación cualitativa:</strong> " + esc(ratingLabel) + "</p>"
                + scoreBlock
                + "<p>Atentamente,<br/>Gestión del Desempeño (GDR)</p>";
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
