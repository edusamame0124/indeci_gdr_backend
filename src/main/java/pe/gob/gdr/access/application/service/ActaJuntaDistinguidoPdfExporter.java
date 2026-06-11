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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

/**
 * Acta de la Junta de Directivos — asignación de Rendimiento distinguido (P6-05, diferido desde P5).
 */
@Service
public class ActaJuntaDistinguidoPdfExporter {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES_PE);
    private static final Color HEADER_BLUE = new Color(0, 32, 96);

    private final FormatoGdrPdfProperties properties;

    public ActaJuntaDistinguidoPdfExporter(FormatoGdrPdfProperties properties) {
        this.properties = properties;
    }

    public byte[] exportPdf(List<GdrResult> distinguidos, String cicloNombre) {
        List<GdrResult> ordenados = distinguidos.stream()
                .sorted(Comparator.comparing(
                        (GdrResult r) -> r.getConsolidatedScore(),
                        Comparator.nullsLast(Comparator.reverseOrder())
                ).thenComparing(r -> r.getAssignment().getEvaluatedPerson().getDisplayName()))
                .toList();
        if (ordenados.isEmpty()) {
            throw new DomainException(
                    "No hay servidores con Rendimiento distinguido en el ciclo activo. "
                            + "Registre la asignación en la Junta antes de generar el acta.");
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(document, buffer);
            document.open();
            document.add(titleBlock());
            document.add(entityBlock(cicloNombre));
            document.add(assignmentsTable(ordenados));
            document.add(footerNote());
            document.close();
            return buffer.toByteArray();
        } catch (DocumentException e) {
            throw new DomainException("No se pudo generar el acta de la Junta en PDF: " + e.getMessage());
        }
    }

    private Paragraph titleBlock() {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f);
        Paragraph title = new Paragraph(
                "ACTA DE JUNTA DE DIRECTIVOS — RENDIMIENTO DISTINGUIDO",
                titleFont
        );
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        return title;
    }

    private Paragraph entityBlock(String cicloNombre) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10f);
        String texto = nullToEmpty(properties.getEntityName())
                + "\nCiclo: " + nullToEmpty(cicloNombre)
                + "\nFecha de emisión: " + DATE_ONLY.format(LocalDate.now());
        Paragraph entity = new Paragraph(texto, font);
        entity.setAlignment(Element.ALIGN_CENTER);
        entity.setSpacingAfter(12);
        return entity;
    }

    private PdfPTable assignmentsTable(List<GdrResult> distinguidos) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {0.6f, 2.4f, 1.2f, 1.8f});
        addHeaderCell(table, "N.°");
        addHeaderCell(table, "Servidor/a evaluado/a");
        addHeaderCell(table, "Puntaje");
        addHeaderCell(table, "Calificación");

        int index = 1;
        for (GdrResult result : distinguidos) {
            table.addCell(dataCell(String.valueOf(index++)));
            table.addCell(dataCell(result.getAssignment().getEvaluatedPerson().getDisplayName()));
            table.addCell(dataCell(
                    result.getConsolidatedScore() != null ? result.getConsolidatedScore().toPlainString() : "—"));
            table.addCell(dataCell(QualitativeRating.labelOf(result.getQualitativeRatingCode())));
        }
        return table;
    }

    private Paragraph footerNote() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f);
        Paragraph note = new Paragraph(
                "Documento de respaldo institucional de la asignación de Rendimiento distinguido. "
                        + "Referencia normativa: " + nullToEmpty(properties.getNormativeReference()) + ".",
                font
        );
        note.setSpacingBefore(16);
        return note;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f)));
        cell.setBackgroundColor(HEADER_BLUE);
        cell.getPhrase().getFont().setColor(Color.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private PdfPCell dataCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nullToEmpty(text), FontFactory.getFont(FontFactory.HELVETICA, 9f)));
        cell.setPadding(5);
        return cell;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
