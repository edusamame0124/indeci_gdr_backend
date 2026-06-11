package pe.gob.gdr.access.application.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

/**
 * Genera el acta de sesión del Comité Institucional de Evaluación (P6-04).
 * Normativa: RPE 068-2020-SERVIR-PE Art. 42.
 */
@Service
public class ActaCiePdfExporter {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ES_PE);
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES_PE);
    private static final Color HEADER_BLUE = new Color(0, 32, 96);

    private final FormatoGdrPdfProperties properties;

    public ActaCiePdfExporter(FormatoGdrPdfProperties properties) {
        this.properties = properties;
    }

    public byte[] exportPdf(GdrCasoCie caso) {
        if (!GdrCasoCie.ESTADO_RESUELTO.equals(caso.getEstado())) {
            throw new DomainException(
                    "El acta del CIE solo puede generarse para casos resueltos. Registre la decisión antes de descargar.");
        }
        GdrSolicitudConfirmacion solicitud = caso.getSolicitud();
        GdrFinalEvaluation evaluation = solicitud.getFinalEvaluation();
        GdrEvaluationAssignment assignment = evaluation.getAssignment();

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(document, buffer);
            document.open();
            document.add(titleBlock());
            document.add(entityBlock());
            document.add(caseSummaryTable(caso, solicitud, assignment, evaluation));
            document.add(decisionBlock(caso));
            document.add(footerNote());
            document.close();
            return buffer.toByteArray();
        } catch (DocumentException e) {
            throw new DomainException("No se pudo generar el acta del CIE en PDF: " + e.getMessage());
        }
    }

    private Paragraph titleBlock() {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f);
        Paragraph title = new Paragraph("ACTA DE SESIÓN — COMITÉ INSTITUCIONAL DE EVALUACIÓN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        return title;
    }

    private Paragraph entityBlock() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10f);
        Paragraph entity = new Paragraph(nullToEmpty(properties.getEntityName()), font);
        entity.setAlignment(Element.ALIGN_CENTER);
        entity.setSpacingAfter(12);
        return entity;
    }

    private PdfPTable caseSummaryTable(
            GdrCasoCie caso,
            GdrSolicitudConfirmacion solicitud,
            GdrEvaluationAssignment assignment,
            GdrFinalEvaluation evaluation
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1.4f, 2.6f});
        addRow(table, "Número de caso", caso.getNumeroCaso());
        addRow(table, "Ciclo", assignment.getCycle().getName());
        addRow(table, "Evaluado", assignment.getEvaluatedPerson().getDisplayName());
        addRow(table, "Evaluador", assignment.getEvaluatorPerson().getDisplayName());
        addRow(table, "Fecha ingreso CIE",
                caso.getFechaIngresoCie() != null ? DATE_TIME.format(caso.getFechaIngresoCie()) : "—");
        addRow(table, "Sustento del evaluado", solicitud.getSustentoEvaluado());
        addRow(table, "Calificación al ingreso",
                QualitativeRating.labelOf(evaluation.getQualitativeRatingCode()));
        addRow(table, "Puntaje consolidado",
                evaluation.getConsolidatedScore() != null ? evaluation.getConsolidatedScore().toPlainString() : "—");
        return table;
    }

    private PdfPTable decisionBlock(GdrCasoCie caso) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(14);

        PdfPCell header = new PdfPCell(new Phrase("DECISIÓN DEFINITIVA DEL CIE", boldFont(10f)));
        header.setBackgroundColor(HEADER_BLUE);
        header.getPhrase().getFont().setColor(Color.WHITE);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(8);
        table.addCell(header);

        String decisionLabel = GdrCasoCie.DECISION_CONFIRMA.equals(caso.getDecision())
                ? "Se confirma la calificación otorgada por el evaluador."
                : "Se modifica la calificación otorgada por el evaluador.";
        StringBuilder body = new StringBuilder(decisionLabel);
        if (GdrCasoCie.DECISION_MODIFICA.equals(caso.getDecision()) && caso.getCalificacionResultado() != null) {
            body.append("\nNueva calificación: ")
                    .append(QualitativeRating.labelOf(caso.getCalificacionResultado()));
        }
        body.append("\nSustento del CIE: ").append(nullToEmpty(caso.getSustentoCie()));
        if (caso.getFechaDecision() != null) {
            body.append("\nFecha de decisión: ").append(DATE_TIME.format(caso.getFechaDecision()));
        }

        PdfPCell content = new PdfPCell(new Phrase(body.toString(), normalFont(9.5f)));
        content.setPadding(10);
        table.addCell(content);
        return table;
    }

    private Paragraph footerNote() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f);
        Paragraph note = new Paragraph(
                "Documento generado conforme a la RPE N.° 068-2020-SERVIR-PE (Art. 42). "
                        + "La decisión del CIE es definitiva.",
                font
        );
        note.setSpacingBefore(16);
        return note;
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont(9f)));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(nullToEmpty(value), normalFont(9f)));
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private static Font boldFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
    }

    private static Font normalFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
