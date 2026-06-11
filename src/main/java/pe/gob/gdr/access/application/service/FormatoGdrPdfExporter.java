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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.domain.repository.GdrSeguimientoRepository;
import pe.gob.gdr.access.infrastructure.config.FormatoGdrPdfProperties;

@Service
public class FormatoGdrPdfExporter {

    private static final float[] MAIN_GRID_WIDTHS = {2.1f, 2.2f, 1.1f, 1.0f, 0.7f, 1.5f, 1.0f, 1.4f, 1.0f, 1.0f};

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter CYCLE_DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy", ES_PE);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES_PE);

    private static final Color HEADER_BLUE = new Color(0, 32, 96);
    private static final Color HEADER_TEAL = new Color(0, 112, 131);
    private static final Color HEADER_GREEN = new Color(0, 128, 90);
    private static final Color HEADER_ORANGE = new Color(228, 108, 10);
    private static final Color LABEL_GREY = new Color(217, 217, 217);
    private final FormatoGdrPdfProperties properties;
    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrImprovementOpportunityRepository improvementRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrSeguimientoRepository seguimientoRepository;

    public FormatoGdrPdfExporter(
            FormatoGdrPdfProperties properties,
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrImprovementOpportunityRepository improvementRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrSeguimientoRepository seguimientoRepository
    ) {
        this.properties = properties;
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.improvementRepository = improvementRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    public byte[] exportPdf(FormatoGdrPdfExportContext ctx) {
        GdrEvaluationAssignment assignment = ctx.assignment();
        Optional<GdrResult> consolidated = ctx.consolidatedResult();
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(document, buffer);
            document.open();
            document.add(buildHeaderBlockWithEntity());
            if (ctx.isDraft()) {
                document.add(draftBannerStrip());
            }
            document.add(personsSection(assignment));
            document.add(authorizationBlock());
            document.add(cycleAndStagesSection(assignment));
            document.add(goalsGrid(assignment));
            document.add(finalResultsSection(assignment, consolidated));
            document.add(finalSignaturesSection());
            document.add(servirFooter());
            document.close();
            return buffer.toByteArray();
        } catch (DocumentException e) {
            throw new DomainException("No se pudo generar el Formato GDR en PDF: " + e.getMessage());
        }
    }

    /**
     * Bloque único cabecera (3 columnas) + fila entidad (2 columnas), sin borde en el contenedor para evitar
     * hueco o doble trazo entre tablas en el layout.
     */
    private PdfPTable buildHeaderBlockWithEntity() throws DocumentException {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(0);

        PdfPCell rowHeader = new PdfPCell(buildFramedInstitutionalHeader());
        rowHeader.setBorder(PdfPCell.NO_BORDER);
        rowHeader.setPadding(0);
        wrapper.addCell(rowHeader);

        PdfPCell rowEntity = new PdfPCell(entityStrip());
        rowEntity.setBorder(PdfPCell.NO_BORDER);
        rowEntity.setPadding(0);
        wrapper.addCell(rowEntity);

        return wrapper;
    }

    private PdfPTable draftBannerStrip() {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(0);
        t.setSpacingAfter(4);
        String note = nullToEmpty(properties.getDraftBannerNote()).trim();
        Paragraph p = new Paragraph(note.isEmpty() ? "BORRADOR" : note, smallFont(7.5f));
        p.setAlignment(Element.ALIGN_CENTER);
        PdfPCell c = new PdfPCell(p);
        c.setBackgroundColor(new Color(255, 250, 205));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        c.setBorder(PdfPCell.BOX);
        c.setBorderWidth(0.75f);
        c.setBorderColor(Color.BLACK);
        t.addCell(c);
        return t;
    }

    /**
     * Encabezado institucional en recuadro: solo logo (izq.), título (centro), versión y SERVIR (der.).
     */
    private PdfPTable buildFramedInstitutionalHeader() throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(0);
        table.setWidths(new float[] {1.35f, 4.3f, 1.35f});

        PdfPCell left = buildLeftHeaderCell();
        applyFramedHeaderCellStyle(left);

        PdfPCell center = buildCenterTitleCell();
        applyFramedHeaderCellStyle(center);

        PdfPCell right = buildRightMetadataCell();
        applyFramedHeaderCellStyle(right);

        table.addCell(left);
        table.addCell(center);
        table.addCell(right);
        return table;
    }

    private void applyFramedHeaderCellStyle(PdfPCell cell) {
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderWidth(1.5f);
        cell.setBorderColor(Color.BLACK);
        cell.setPadding(8);
    }

    private PdfPCell buildLeftHeaderCell() {
        PdfPCell empty = new PdfPCell(new Phrase(" "));
        empty.setBorder(PdfPCell.NO_BORDER);
        empty.setMinimumHeight(52f);
        return empty;
    }

    private PdfPCell buildCenterTitleCell() {
        String titleText = "FORMATO PARA LA GESTIÓN DEL RENDIMIENTO".toUpperCase(ES_PE);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f);
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        PdfPCell cell = new PdfPCell(title);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell buildRightMetadataCell() {
        String body = formatServirHeaderLines(properties.getServirFooterNote());
        String normativa = nullToEmpty(properties.getNormativeReference()).trim();
        String block = properties.getFormVersion()
                + "\n"
                + properties.getFormRevision()
                + (normativa.isBlank() ? "" : "\n" + normativa)
                + (body.isBlank() ? "" : "\n" + body);
        Phrase phrase = new Phrase(block, smallFont(5f));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        return cell;
    }

    /**
     * Parte el pie SERVIR en dos renglones tipo encabezado institucional (DEL / SERVICIO CIVIL…).
     */
    private static String formatServirHeaderLines(String servirNote) {
        if (servirNote == null || servirNote.isBlank()) {
            return "";
        }
        String t = servirNote.trim();
        String needle = " SERVICIO CIVIL";
        int idx = t.indexOf(needle);
        if (idx > 0) {
            return t.substring(0, idx).trim() + "\n" + t.substring(idx).trim();
        }
        return t;
    }

    /**
     * Fila nombre de entidad: recuadro índigo (etiqueta) | recuadro blanco (valor centrado, mayúsculas/minúsculas como en configuración).
     */
    private PdfPTable entityStrip() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        try {
            table.setWidths(new float[] {0.72f, 3.28f});
        } catch (DocumentException ignored) {
            //
        }

        PdfPCell label = cellColored("NOMBRE DE LA ENTIDAD", HEADER_BLUE, Color.WHITE, Element.ALIGN_CENTER, 8f);
        label.setBorder(PdfPCell.BOX);
        label.setBorderWidth(1.5f);
        label.setBorderColor(Color.BLACK);
        label.setVerticalAlignment(Element.ALIGN_MIDDLE);
        label.setPadding(10);

        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10f);
        String entityValue = nullToEmpty(properties.getEntityName()).trim();
        Paragraph valuePara = new Paragraph(entityValue.isEmpty() ? " " : entityValue, valueFont);
        valuePara.setAlignment(Element.ALIGN_CENTER);
        PdfPCell value = new PdfPCell(valuePara);
        value.setBackgroundColor(Color.WHITE);
        value.setHorizontalAlignment(Element.ALIGN_CENTER);
        value.setVerticalAlignment(Element.ALIGN_MIDDLE);
        value.setBorder(PdfPCell.BOX);
        value.setBorderWidth(1.5f);
        value.setBorderColor(Color.BLACK);
        value.setPadding(12);

        table.addCell(label);
        table.addCell(value);
        return table;
    }

    private PdfPTable personsSection(GdrEvaluationAssignment assignment) {
        HrPerson evaluated = assignment.getEvaluatedPerson();
        HrPerson evaluator = assignment.getEvaluatorPerson();
        String segmentEvaluated = FormatoGdrExportMappings.resolveEvaluatedSegmentDisplayLiteral(
                assignment,
                properties.getSegmentDisplayAliases()
        );
        String segmentEvaluator = FormatoGdrExportMappings.SELECT_PLACEHOLDER;

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(6);

        table.addCell(sectionHeaderCell("DATOS GENERALES - SERVIDOR/A EVALUADO/A", HEADER_BLUE));
        table.addCell(sectionHeaderCell("DATOS GENERALES - SERVIDOR/A EVALUADOR/A", HEADER_BLUE));

        table.addCell(personTwoCol("DNI:", nullToEmpty(evaluated.getDocumentNumber())));
        table.addCell(personTwoCol("DNI:", nullToEmpty(evaluator.getDocumentNumber())));
        table.addCell(personTwoCol("APELLIDOS Y NOMBRES:", nullToUpper(evaluated.getDisplayName())));
        table.addCell(personTwoCol("APELLIDOS Y NOMBRES:", nullToUpper(evaluator.getDisplayName())));
        table.addCell(personTwoCol("PUESTO:", resolvePersonCargo(evaluated)));
        table.addCell(personTwoCol("PUESTO:", resolvePersonCargo(evaluator)));
        table.addCell(personTwoCol("NIVEL REMUNERATIVO:", resolveNivelRemunerativo(evaluated)));
        table.addCell(personTwoCol("NIVEL REMUNERATIVO:", resolveNivelRemunerativo(evaluator)));
        table.addCell(personTwoCol("SEGMENTO:", segmentEvaluated));
        table.addCell(personTwoCol("SEGMENTO:", segmentEvaluator));
        table.addCell(personTwoCol(
                "NOMBRE DE LA UNIDAD DE ORGANIZACION:",
                orgUnitName(evaluated)
        ));
        table.addCell(personTwoCol(
                "NOMBRE DE LA UNIDAD DE ORGANIZACION:",
                orgUnitName(evaluator)
        ));
        return table;
    }

    private PdfPTable authorizationBlock() {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        String text =
                "Se deja constancia que la suscrita autoriza la notificacion por correo electronico institucional, "
                        + "conforme a lo dispuesto por la Ley N 27444 del Procedimiento Administrativo General y normas "
                        + "conexas.";
        PdfPCell c = new PdfPCell(new Phrase(text, smallFont(7.5f)));
        c.setPadding(6);
        t.addCell(c);

        PdfPTable sig = new PdfPTable(2);
        sig.setWidthPercentage(100);
        sig.setSpacingBefore(4);
        PdfPCell left = signaturePlaceholderCell("Espacio para firma digital - EVALUADO/A");
        PdfPCell right = signaturePlaceholderCell("Espacio para firma digital - EVALUADOR/A");
        sig.addCell(left);
        sig.addCell(right);
        t.addCell(sig);
        return t;
    }

    private PdfPTable cycleAndStagesSection(GdrEvaluationAssignment assignment) {
        ActiveCycle cycle = assignment.getCycle();
        PdfPTable wrap = new PdfPTable(1);
        wrap.setWidthPercentage(100);
        wrap.setSpacingBefore(8);

        wrap.addCell(sectionHeaderCell("CICLO DE LA GESTION DEL RENDIMIENTO", HEADER_BLUE));

        PdfPTable stages = new PdfPTable(3);
        stages.setWidthPercentage(100);
        try {
            stages.setWidths(new float[] {1f, 1f, 1f});
        } catch (DocumentException ignored) {
            //
        }
        String fechaFijacionMetas = resolveFechaFijacionMetasLiteral(assignment);
        String asistioFijacion = tieneReunionSeguimiento(assignment.getId()) ? "Si" : "—";
        stages.addCell(stageCell(
                "ETAPA DE PLANIFICACION",
                HEADER_TEAL,
                "¿El servidor asistio a la reunion de fijacion de metas?\n" + fechaFijacionMetas,
                asistioFijacion,
                "Fecha de la fijacion de metas\n" + fechaFijacionMetas
        ));
        stages.addCell(stageCell(
                "ETAPA DE SEGUIMIENTO",
                HEADER_GREEN,
                "¿Al cierre de esta etapa, el servidor presento evidencias que permitan su evaluacion "
                        + "(al menos de una meta)?",
                deriveEvidencePresenceHint(assignment.getId()),
                ""
        ));
        stages.addCell(stageCell(
                "ETAPA DE EVALUACION",
                HEADER_ORANGE,
                "VALOR ALCANZADO / PUNTAJE POR META",
                "Ver tabla siguiente",
                ""
        ));
        PdfPCell stagesWrapped = new PdfPCell(stages);
        stagesWrapped.setPadding(0);
        stagesWrapped.setBorder(PdfPCell.BOX);
        wrap.addCell(stagesWrapped);

        PdfPCell summary = new PdfPCell(new Phrase(
                nullToEmpty(cycle.getCode())
                        + " · "
                        + nullToEmpty(cycle.getName())
                        + " | Vigencia: "
                        + formatCycleRange(cycle.getStartDate(), cycle.getEndDate())
                        + " | Ventana calificacion final: "
                        + formatOptionalRange(cycle.getFinalEvalGradeStartDate(), cycle.getFinalEvalGradeEndDate())
                        + " | Limite notificacion cualitativa: "
                        + formatOptionalDay(cycle.getQualNotifyDeadlineDate()),
                smallFont(7.5f)
        ));
        summary.setPadding(6);
        wrap.addCell(summary);
        return wrap;
    }

    private String deriveEvidencePresenceHint(Long assignmentId) {
        List<GdrEvidence> all = evidenceRepository.findActiveByGoalAssignmentIdInActiveCycle(assignmentId);
        return all.isEmpty() ? "No" : "Si";
    }

    private PdfPTable goalsGrid(GdrEvaluationAssignment assignment) {
        PdfPTable table = new PdfPTable(MAIN_GRID_WIDTHS.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        try {
            table.setWidths(MAIN_GRID_WIDTHS);
        } catch (DocumentException ignored) {
            //
        }

        table.addCell(headerCell("PRIORIDADES / META"));
        table.addCell(headerCell("INDICADOR / PRODUCTO"));
        table.addCell(headerCell("SENTIDO"));
        table.addCell(headerCell("VALOR ESPERADO"));
        table.addCell(headerCell("PESO"));
        table.addCell(headerCell("EVIDENCIA"));
        table.addCell(headerCell("PLAZOS"));
        table.addCell(headerCell("SEGUIMIENTO"));
        table.addCell(headerCell("VALOR ALCANZADO"));
        table.addCell(headerCell("PUNTAJE META"));

        List<GdrGoal> goals = new ArrayList<>(
                goalRepository.findActiveGoalsByAssignmentIdInActiveCycle(assignment.getId())
        );
        goals.sort(Comparator.comparing(GdrGoal::getId));

        for (GdrGoal goal : goals) {
            List<GdrEvidence> evidences = evidenceRepository.findActiveByGoalIdInActiveCycle(goal.getId());
            int subRows = evidences.isEmpty() ? 1 : evidences.size();
            boolean first = true;
            if (evidences.isEmpty()) {
                addGoalRowCells(table, goal, null, subRows, true);
                first = false;
            } else {
                for (GdrEvidence evidence : evidences) {
                    addGoalRowCells(table, goal, evidence, subRows, first);
                    first = false;
                }
            }
        }
        return table;
    }

    private void addGoalRowCells(
            PdfPTable table,
            GdrGoal goal,
            GdrEvidence evidence,
            int subRows,
            boolean firstRowOfGoal
    ) {
        if (firstRowOfGoal) {
            PdfPCell cTitle = dataCell(nullToEmpty(goal.getTitle()));
            cTitle.setRowspan(subRows);
            table.addCell(cTitle);

            PdfPCell cInd = dataCell(nullToEmpty(goal.getIndicator().getName()));
            cInd.setRowspan(subRows);
            table.addCell(cInd);

            PdfPCell cSent = dataCell(FormatoGdrExportMappings.mapIndicatorFormulaToSentido(goal));
            cSent.setRowspan(subRows);
            table.addCell(cSent);

            PdfPCell cExp = dataCell(formatPercent(goal.getExpectedValue()));
            cExp.setRowspan(subRows);
            table.addCell(cExp);

            PdfPCell cPeso = dataCell(formatPercent(goal.getWeight()));
            cPeso.setRowspan(subRows);
            table.addCell(cPeso);
        }

        if (evidence == null) {
            table.addCell(dataCell("No presenta evidencia"));
            table.addCell(dataCell(formatGoalPlazo(goal)));
            table.addCell(dataCell(FormatoGdrExportMappings.mapSeguimientoLiteral(null)));
        } else {
            table.addCell(dataCell(evidenceText(evidence)));
            table.addCell(dataCell(formatEvidencePlazo(evidence, goal)));
            String seg = FormatoGdrExportMappings.mapSeguimientoLiteral(evidence)
                    + "\n"
                    + FormatoGdrExportMappings.mapEvidenciaFinalLiteral(evidence);
            table.addCell(dataCell(seg));
        }

        if (firstRowOfGoal) {
            PdfPCell cAch = dataCell(formatDecimal(goal.getAchievedValue()));
            cAch.setRowspan(subRows);
            table.addCell(cAch);

            PdfPCell cScore = dataCell(formatDecimal(goal.getCalculatedScore()));
            cScore.setRowspan(subRows);
            table.addCell(cScore);
        }
    }

    private PdfPTable finalResultsSection(GdrEvaluationAssignment assignment, Optional<GdrResult> resultOpt) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);
        outer.setSpacingBefore(10);
        outer.addCell(sectionHeaderCell("RESULTADOS FINALES", HEADER_BLUE));

        PdfPTable grid = new PdfPTable(4);
        grid.setWidthPercentage(100);
        try {
            grid.setWidths(new float[] {1.5f, 0.85f, 1.0f, 2.15f});
        } catch (DocumentException ignored) {
            //
        }

        grid.addCell(subHeaderCell("CRITERIOS PARA LA CALIFICACION"));
        grid.addCell(subHeaderCell("PUNTUACION FINAL"));
        grid.addCell(subHeaderCell("CALIFICACION"));
        PdfPCell retroHead = subHeaderCell("REUNION DE RETROALIMENTACION FINAL Y ACCIONES DE MEJORA");
        grid.addCell(retroHead);

        GdrResult result = resultOpt.orElse(null);
        String directive82Line =
                result == null
                        ? "—"
                        : FormatoGdrExportMappings.mapDirective82ToBook(result.getDirective82Compliance());
        String criteriaBody =
                "1) PERMANENCIA EN EL PUESTO ANTES DE EMPEZAR LA ETAPA DE EVALUACION "
                        + "(Tiene 6 meses o mas desempenando el puesto que es objeto de evaluacion): —\n\n"
                        + "2) LA JUNTA DE DIRECTIVOS OTORGO RENDIMIENTO DISTINGUIDO AL SERVIDOR: "
                        + distinguishedLiteral(result != null ? result.getQualitativeRatingCode() : null)
                        + "\n\n"
                        + "3) Ordinal 8.2 directiva GDR (cumplimiento registrado): "
                        + directive82Line;
        PdfPCell crit = dataCell(criteriaBody);
        crit.setBackgroundColor(new Color(255, 242, 230));
        grid.addCell(crit);

        BigDecimal consolidated =
                result != null ? result.getConsolidatedScore() : null;
        grid.addCell(dataCell(formatDecimal(consolidated)));
        grid.addCell(
                dataCell(
                        FormatoGdrExportMappings.qualitativeRatingLabel(
                                result != null ? result.getQualitativeRatingCode() : null
                        )
                )
        );

        String improvementText = buildImprovementNarrative(assignment.getEvaluatedPerson().getId());
        grid.addCell(dataCell(improvementText));

        // P3 — Formato 2025 (RPE 000041-2025/PE): fecha real de la reunión si está registrada
        PdfPCell dateRow = new PdfPCell(new Phrase(
                "FECHA DE REUNION DE RETROALIMENTACION FINAL: " + resolveRetroFinalDateLiteral(assignment),
                smallFont(7.5f)
        ));
        dateRow.setColspan(4);
        dateRow.setPadding(6);
        grid.addCell(dateRow);

        PdfPCell wrap = new PdfPCell(grid);
        wrap.setPadding(0);
        outer.addCell(wrap);
        return outer;
    }

    private String resolveRetroFinalDateLiteral(GdrEvaluationAssignment assignment) {
        return finalEvaluationRepository.findByAssignmentIdInActiveCycle(assignment.getId())
                .map(GdrFinalEvaluation::getFechaReunionRetroFinal)
                .map(SHORT_DATE::format)
                .orElse("[dd/mmm/aaaa]");
    }

    private String resolvePersonCargo(HrPerson person) {
        String cargo = person.getCargo();
        return cargo == null || cargo.isBlank() ? "—" : cargo.trim();
    }

    private String resolveNivelRemunerativo(HrPerson person) {
        String nivel = person.getNivelRemunerativo();
        return nivel == null || nivel.isBlank() ? "—" : nivel.trim();
    }

    private boolean tieneReunionSeguimiento(Long assignmentId) {
        return !seguimientoRepository.findByAssignmentIdOrderByFechaReunion(assignmentId).isEmpty();
    }

    private String resolveFechaFijacionMetasLiteral(GdrEvaluationAssignment assignment) {
        List<GdrSeguimiento> reuniones =
                seguimientoRepository.findByAssignmentIdOrderByFechaReunion(assignment.getId());
        if (!reuniones.isEmpty()) {
            LocalDate fecha = reuniones.get(0).getFechaReunion();
            if (fecha != null) {
                return CYCLE_DAY_FORMAT.format(fecha);
            }
        }
        ActiveCycle cycle = assignment.getCycle();
        if (cycle != null && cycle.getStartDate() != null) {
            return CYCLE_DAY_FORMAT.format(cycle.getStartDate());
        }
        return "[dd/mmm/aaaa]";
    }

    private String distinguishedLiteral(String qualitativeCode) {
        return qualitativeCode != null && "DISTINGUIDO".equalsIgnoreCase(qualitativeCode.trim()) ? "Si" : "No";
    }

    private String buildImprovementNarrative(Long evaluatedPersonId) {
        List<GdrImprovementOpportunity> list =
                improvementRepository.findActiveByEvaluatedIdInActiveCycle(evaluatedPersonId);
        if (list.isEmpty()) {
            return "ACCIONES DE CAPACITACION: —\nOTRAS ACCIONES: —";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ACCIONES DE CAPACITACION / MEJORA (oportunidades registradas):\n");
        for (GdrImprovementOpportunity o : list) {
            sb.append("- ")
                    .append(nullToEmpty(o.getDescription()))
                    .append(" (Responsable: ")
                    .append(nullToEmpty(o.getResponsible()))
                    .append(", Plazo: ")
                    .append(o.getTargetDate() != null ? SHORT_DATE.format(o.getTargetDate()) : "—")
                    .append(")\n");
        }
        return sb.toString();
    }

    private PdfPTable finalSignaturesSection() {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);
        outer.setSpacingBefore(8);
        outer.addCell(sectionHeaderCell(
                "FIRMAS DE LA REUNION DE RETROALIMENTACION FINAL (En etapa de Evaluacion)",
                HEADER_BLUE
        ));
        PdfPTable s = new PdfPTable(2);
        s.setWidthPercentage(100);
        PdfPCell l = signatureFooterCell("FIRMA EVALUADO/A");
        PdfPCell r = signatureFooterCell("FIRMA EVALUADOR/A");
        s.addCell(l);
        s.addCell(r);
        outer.addCell(s);
        return outer;
    }

    private Paragraph servirFooter() {
        Font f = smallFont(6.5f);
        f.setColor(HEADER_BLUE.getRed(), HEADER_BLUE.getGreen(), HEADER_BLUE.getBlue());
        String txt =
                "La version descargable del formato se encuentra en las herramientas de la Gestion del Rendimiento, "
                        + "disponible en la pagina web de SERVIR.";
        Paragraph p = new Paragraph(txt, f);
        p.setSpacingBefore(12);
        return p;
    }

    private PdfPCell stageCell(String title, Color titleBg, String q1, String a1, String extra) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);
        inner.addCell(sectionHeaderCell(title, titleBg));
        PdfPCell body = new PdfPCell(new Phrase(q1 + "\nRespuesta: " + a1 + "\n" + extra, smallFont(7f)));
        body.setPadding(5);
        inner.addCell(body);
        PdfPCell w = new PdfPCell(inner);
        w.setPadding(0);
        return w;
    }

    private PdfPCell sectionHeaderCell(String text, Color bg) {
        return cellColored(text, bg, Color.WHITE, Element.ALIGN_CENTER, 8f);
    }

    private PdfPCell headerCell(String text) {
        return cellColored(text, HEADER_BLUE, Color.WHITE, Element.ALIGN_CENTER, 7f);
    }

    private PdfPCell subHeaderCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, smallBoldFont(7f)));
        c.setBackgroundColor(LABEL_GREY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(4);
        return c;
    }

    private PdfPCell cellColored(String text, Color bg, Color fg, int align, float size) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
        f.setColor(fg);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5);
        return c;
    }

    private PdfPCell personTwoCol(String label, String value) {
        PdfPTable t = new PdfPTable(2);
        try {
            t.setWidths(new float[] {0.9f, 2.1f});
        } catch (DocumentException ignored) {
            //
        }
        Font lf = smallBoldFont(7.5f);
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        l.setBorder(PdfPCell.BOX);
        l.setBackgroundColor(LABEL_GREY);
        l.setPadding(4);
        PdfPCell v = new PdfPCell(new Phrase(nullToEmpty(value), normalFont(8)));
        v.setBorder(PdfPCell.BOX);
        v.setPadding(4);
        t.addCell(l);
        t.addCell(v);
        PdfPCell w = new PdfPCell(t);
        w.setPadding(0);
        return w;
    }

    private PdfPCell signaturePlaceholderCell(String title) {
        PdfPCell c = new PdfPCell(new Phrase(title + "\n\n\n", smallFont(7.5f)));
        c.setMinimumHeight(72f);
        c.setPadding(6);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private PdfPCell signatureFooterCell(String label) {
        PdfPTable t = new PdfPTable(1);
        PdfPCell top = new PdfPCell(new Phrase(label, smallBoldFont(8)));
        top.setHorizontalAlignment(Element.ALIGN_CENTER);
        top.setBackgroundColor(LABEL_GREY);
        top.setPadding(6);
        t.addCell(top);
        PdfPCell box = new PdfPCell(new Phrase(""));
        box.setMinimumHeight(64f);
        t.addCell(box);
        PdfPCell w = new PdfPCell(t);
        w.setPadding(0);
        return w;
    }

    private PdfPCell dataCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", smallFont(7f)));
        c.setPadding(3);
        return c;
    }

    private static Font normalFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size);
    }

    private static Font smallFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size);
    }

    private static Font smallBoldFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
    }

    private static String nullToEmpty(String s) {
        return Objects.requireNonNullElse(s, "");
    }

    private static String nullToUpper(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        return s.toUpperCase(ES_PE);
    }

    private static String orgUnitName(HrPerson person) {
        if (person.getOrgUnit() == null) {
            return "";
        }
        return nullToUpper(person.getOrgUnit().getName());
    }

    private static String formatCycleRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null && end == null) {
            return "";
        }
        String a = start != null ? CYCLE_DAY_FORMAT.format(start).toUpperCase(ES_PE) : "";
        String b = end != null ? CYCLE_DAY_FORMAT.format(end).toUpperCase(ES_PE) : "";
        return a + " — " + b;
    }

    private static String formatOptionalRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null && end == null) {
            return "—";
        }
        return formatCycleRange(start, end);
    }

    private static String formatOptionalDay(java.time.LocalDate day) {
        if (day == null) {
            return "—";
        }
        return CYCLE_DAY_FORMAT.format(day).toUpperCase(ES_PE);
    }

    private static String formatPercent(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private static String evidenceText(GdrEvidence evidence) {
        String t = nullToEmpty(evidence.getTitle());
        String d = evidence.getDetail();
        if (d != null && !d.isBlank()) {
            String shortD = d.length() > 180 ? d.substring(0, 177) + "..." : d;
            return t + "\n" + shortD;
        }
        return t;
    }

    private static String formatEvidencePlazo(GdrEvidence evidence, GdrGoal goal) {
        if (evidence.getExpectedDate() != null) {
            return SHORT_DATE.format(evidence.getExpectedDate());
        }
        return formatGoalPlazo(goal);
    }

    private static String formatGoalPlazo(GdrGoal goal) {
        if (goal.getStartDate() != null && goal.getEndDate() != null) {
            return SHORT_DATE.format(goal.getStartDate()) + " — " + SHORT_DATE.format(goal.getEndDate());
        }
        return "—";
    }

}
