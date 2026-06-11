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
import pe.gob.gdr.access.domain.model.GdrInformeCierre;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

@Service
public class FormatoInformeCierrePdfExporter {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ES_PE);
    private static final Color HEADER = new Color(0, 32, 96);

    private final FormatoGdrPdfProperties properties;

    public FormatoInformeCierrePdfExporter(FormatoGdrPdfProperties properties) {
        this.properties = properties;
    }

    public byte[] exportPdf(GdrInformeCierre informe) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(document, buffer);
            document.open();
            document.add(title());
            document.add(entityLine(informe));
            document.add(statsTable(informe));
            if (informe.getObservacionesOrh() != null && !informe.getObservacionesOrh().isBlank()) {
                document.add(observaciones(informe.getObservacionesOrh()));
            }
            document.add(footerNote());
            document.close();
            return buffer.toByteArray();
        } catch (DocumentException e) {
            throw new DomainException("No se pudo generar el informe de cierre en PDF: " + e.getMessage());
        }
    }

    private Paragraph title() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f);
        Paragraph p = new Paragraph("INFORME DE CIERRE DEL CICLO GDR", font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(10);
        return p;
    }

    private Paragraph entityLine(GdrInformeCierre informe) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10f);
        String text = nullToEmpty(properties.getEntityName())
                + "\nCiclo: " + informe.getCycle().getName()
                + "\nEstado: " + informe.getEstado()
                + "\nGenerado: " + DATE_TIME.format(informe.getFechaGeneracion())
                + " por " + informe.getGeneradoPor();
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(14);
        return p;
    }

    private PdfPTable statsTable(GdrInformeCierre informe) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1.6f, 1f});
        addRow(table, "Total evaluados", String.valueOf(informe.getTotalEvaluados()));
        addRow(table, "Buen rendimiento", String.valueOf(informe.getTotalBuenRendimiento()));
        addRow(table, "Sujeto a observación", String.valueOf(informe.getTotalSujetoObservacion()));
        addRow(table, "Desaprobado", String.valueOf(informe.getTotalDesaprobado()));
        addRow(table, "Rendimiento distinguido", String.valueOf(informe.getTotalDistinguido()));
        addRow(table, "Oportunidades de mejora", String.valueOf(informe.getTotalOportunidadesMejora()));
        addRow(table, "Confirmaciones solicitadas", String.valueOf(informe.getTotalConfirmaciones()));
        addRow(table, "Confirmaciones resueltas", String.valueOf(informe.getTotalConfirmacionesResueltas()));
        addRow(table, "Documentos firmados", String.valueOf(informe.getTotalDocumentosFirmados()));
        return table;
    }

    private Paragraph observaciones(String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);
        Paragraph p = new Paragraph("Observaciones ORH:\n" + texto.trim(), font);
        p.setSpacingBefore(12);
        return p;
    }

    private Paragraph footerNote() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f);
        return new Paragraph(
                "Documento conforme a RPE N.° 068-2020-SERVIR-PE Art. 55. Plazo de remisión: 31 de mayo del año siguiente al ciclo.",
                font
        );
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f)));
        l.setBackgroundColor(new Color(240, 240, 240));
        l.setPadding(6);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 9f)));
        v.setPadding(6);
        table.addCell(v);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
