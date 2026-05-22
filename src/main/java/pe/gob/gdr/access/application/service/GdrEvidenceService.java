package pe.gob.gdr.access.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.gdr.access.application.dto.request.GuardarEvidenciaRequest;
import pe.gob.gdr.access.application.dto.request.RevisionEvidenciaRequest;
import pe.gob.gdr.access.application.dto.response.DetalleEvidenciaResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvidenciaResponse;
import pe.gob.gdr.access.application.dto.response.RevisionEvidenciaResponse;
import pe.gob.gdr.access.application.port.DocumentStoragePort;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrCorrectiveAction;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrEvidenceReview;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.repository.GdrCorrectiveActionRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceReviewRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceStatusRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.infrastructure.config.DocumentStorageProperties;

@Service
public class GdrEvidenceService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_OBSERVED = "OBSERVED";
    private static final String STATUS_SUBSANATED = "SUBSANATED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final Map<String, String> EVIDENCE_TYPE_NAMES = Map.of(
            "AVANCE", "Evidencia de avance",
            "FINAL", "Evidencia final"
    );
    private static final Map<String, String> EXPECTED_FORMAT_NAMES = Map.of(
            "IMAGEN", "Imagen (JPG, PNG)",
            "DOCUMENTO", "Documento (PDF, Word)",
            "ACTA", "Acta (PDF)",
            "EXCEL", "Archivo Excel",
            "OTRO", "Otro formato"
    );
    private static final Map<String, String> QUALIFICATION_LABELS = Map.ofEntries(
            Map.entry("LOGRADO", "Logrado"),
            Map.entry("EN_PROCESO_LOGRO", "En proceso de logro"),
            Map.entry("NO_PRESENTA_EVIDENCIA", "No presenta evidencia"),
            Map.entry("PRESENTA_EVIDENCIA_FINAL", "Sí presenta evidencia final"),
            Map.entry("NO_PRESENTA_EVIDENCIA_LOGRO_FINAL", "No presenta evidencia de logro final"),
            Map.entry("OBSERVADO", "Observado")
    );
    private static final Set<String> ADVANCE_QUALIFICATIONS = Set.of(
            "LOGRADO",
            "EN_PROCESO_LOGRO",
            "NO_PRESENTA_EVIDENCIA",
            "OBSERVADO"
    );
    private static final Set<String> FINAL_QUALIFICATIONS = Set.of(
            "PRESENTA_EVIDENCIA_FINAL",
            "NO_PRESENTA_EVIDENCIA_LOGRO_FINAL",
            "OBSERVADO"
    );
    private static final Set<String> APPROVED_QUALIFICATIONS = Set.of("LOGRADO", "PRESENTA_EVIDENCIA_FINAL");
    private static final String EVIDENCE_FILE_CATEGORY = "evidencias";
    private static final Map<String, Set<String>> EXTENSIONS_BY_FORMAT = Map.of(
            "IMAGEN", Set.of(".jpg", ".jpeg", ".png"),
            "DOCUMENTO", Set.of(".pdf", ".doc", ".docx"),
            "ACTA", Set.of(".pdf"),
            "EXCEL", Set.of(".xls", ".xlsx"),
            "OTRO", Set.of(".jpg", ".jpeg", ".png", ".pdf", ".doc", ".docx", ".xls", ".xlsx")
    );
    private static final Map<String, Set<String>> MIME_TYPES_BY_FORMAT = Map.of(
            "IMAGEN", Set.of("image/jpeg", "image/png"),
            "DOCUMENTO", Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/octet-stream"
            ),
            "ACTA", Set.of("application/pdf", "application/octet-stream"),
            "EXCEL", Set.of(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/octet-stream"
            ),
            "OTRO", Set.of(
                    "image/jpeg",
                    "image/png",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/octet-stream"
            )
    );

    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrEvidenceStatusRepository evidenceStatusRepository;
    private final GdrEvidenceReviewRepository evidenceReviewRepository;
    private final GdrCorrectiveActionRepository correctiveActionRepository;
    private final DocumentStoragePort documentStoragePort;
    private final DocumentStorageProperties storageProperties;

    public GdrEvidenceService(
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrEvidenceStatusRepository evidenceStatusRepository,
            GdrEvidenceReviewRepository evidenceReviewRepository,
            GdrCorrectiveActionRepository correctiveActionRepository,
            DocumentStoragePort documentStoragePort,
            DocumentStorageProperties storageProperties
    ) {
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceStatusRepository = evidenceStatusRepository;
        this.evidenceReviewRepository = evidenceReviewRepository;
        this.correctiveActionRepository = correctiveActionRepository;
        this.documentStoragePort = documentStoragePort;
        this.storageProperties = storageProperties;
    }

    public List<ResumenEvidenciaResponse> listGoalEvidences(Long goalId) {
        ensureActiveGoal(goalId);
        return evidenceRepository.findActiveByGoalIdInActiveCycle(goalId).stream()
                .map(this::mapSummary)
                .toList();
    }

    public DetalleEvidenciaResponse getEvidence(Long evidenceId) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        return mapDetail(evidence);
    }

    @Transactional
    public DetalleEvidenciaResponse createEvidence(Long goalId, GuardarEvidenciaRequest request, MultipartFile file) {
        GdrGoal goal = ensureActiveGoal(goalId);
        GdrEvidence evidence = new GdrEvidence();
        evidence.setGoal(goal);
        evidence.setEvidenceStatus(resolveStatus(STATUS_REGISTERED));
        evidence.setStatus("ACTIVE");
        applyEvidenceRequest(evidence, request);
        applyEvidenceFile(evidence, file);
        return mapDetail(evidenceRepository.save(evidence));
    }

    @Transactional
    public DetalleEvidenciaResponse updateEvidence(Long evidenceId, GuardarEvidenciaRequest request, MultipartFile file) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        applyEvidenceRequest(evidence, request);
        applyEvidenceFile(evidence, file);

        if (STATUS_OBSERVED.equalsIgnoreCase(evidence.getEvidenceStatus().getStatusCode())) {
            evidence.setEvidenceStatus(resolveStatus(STATUS_SUBSANATED));
            closeOpenCorrectiveActions(evidenceId);
        }

        return mapDetail(evidenceRepository.save(evidence));
    }

    @Transactional
    public DetalleEvidenciaResponse reviewEvidence(Long evidenceId, RevisionEvidenciaRequest request) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        String qualificationCode = normalizeRequired(request.qualificationCode(), "La calificacion de la evidencia es obligatoria.")
                .toUpperCase(Locale.ROOT);
        validateQualificationForEvidenceType(qualificationCode, evidence.getEvidenceTypeCode());
        String decisionCode = resolveDecisionFromQualification(qualificationCode);

        GdrEvidenceStatus evidenceStatus = resolveStatus(decisionCode);
        String reviewComment = normalizeOptional(request.comment());
        String correctiveActionDetail = normalizeOptional(request.correctiveActionDetail());

        if (STATUS_OBSERVED.equalsIgnoreCase(decisionCode)) {
            if (reviewComment == null) {
                throw new DomainException("La revision observada requiere un comentario.");
            }
            if (correctiveActionDetail == null) {
                throw new DomainException("La revision observada requiere una accion correctiva.");
            }
        }

        GdrEvidenceReview savedReview = evidenceReviewRepository.save(GdrEvidenceReview.builder()
                .evidence(evidence)
                .evidenceStatus(evidenceStatus)
                .reviewComment(reviewComment)
                .qualificationCode(qualificationCode)
                .build());

        evidence.setEvidenceStatus(evidenceStatus);
        evidenceRepository.save(evidence);

        if (STATUS_OBSERVED.equalsIgnoreCase(decisionCode)) {
            closeOpenCorrectiveActions(evidenceId);
            correctiveActionRepository.save(GdrCorrectiveAction.builder()
                    .evidenceReview(savedReview)
                    .evidence(evidence)
                    .actionDetail(correctiveActionDetail)
                    .actionStatus("OPEN")
                    .build());
        } else {
            closeOpenCorrectiveActions(evidenceId);
        }

        return mapDetail(getActiveEvidence(evidenceId));
    }

    public ResponseEntity<Resource> downloadEvidenceFile(Long evidenceId, boolean download) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        if (evidence.getFileKey() == null || evidence.getFileKey().isBlank()) {
            throw new ResourceNotFoundException("La evidencia no tiene archivo adjunto.");
        }
        Resource resource = documentStoragePort.loadAsResource(evidence.getFileKey());
        ContentDisposition contentDisposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(evidence.getFileOriginalName() == null ? "evidencia" : evidence.getFileOriginalName())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(
                        evidence.getFileMimeType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : evidence.getFileMimeType()
                ))
                .body(resource);
    }

    private GdrGoal ensureActiveGoal(Long goalId) {
        return goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro una meta activa para la evidencia."));
    }

    private GdrEvidence getActiveEvidence(Long evidenceId) {
        return evidenceRepository.findActiveByIdInActiveCycle(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la evidencia solicitada."));
    }

    private GdrEvidenceStatus resolveStatus(String statusCode) {
        return evidenceStatusRepository.findActiveByCode(statusCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el estado de evidencia requerido."));
    }

    private void applyEvidenceRequest(GdrEvidence evidence, GuardarEvidenciaRequest request) {
        evidence.setTitle(normalizeRequired(request.title(), "El titulo de la evidencia es obligatorio."));
        evidence.setDetail(normalizeOptional(request.detail()));
        evidence.setEvidenceTypeCode(normalizeCatalogCode(
                request.evidenceTypeCode(),
                EVIDENCE_TYPE_NAMES,
                "El tipo de evidencia es obligatorio.",
                "El tipo de evidencia no es valido."
        ));
        evidence.setExpectedFormatCode(normalizeCatalogCode(
                request.expectedFormatCode(),
                EXPECTED_FORMAT_NAMES,
                "El formato esperado es obligatorio.",
                "El formato esperado no es valido."
        ));
        evidence.setExpectedDate(request.expectedDate());
    }

    private void applyEvidenceFile(GdrEvidence evidence, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        validateEvidenceFile(file, evidence.getExpectedFormatCode());
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extensionFrom(originalName);
        try {
            byte[] content = file.getBytes();
            String fileKey = documentStoragePort.store(EVIDENCE_FILE_CATEGORY, extension, content);
            evidence.setFileKey(fileKey);
            evidence.setFileOriginalName(originalName);
            evidence.setFileMimeType(normalizeMimeType(file.getContentType()));
            evidence.setFileSizeBytes(file.getSize());
            evidence.setFileUploadedAt(LocalDateTime.now());
        } catch (IOException exception) {
            throw new DomainException("No se pudo leer el archivo de evidencia adjunto.");
        }
    }

    private void validateEvidenceFile(MultipartFile file, String expectedFormatCode) {
        if (file.getSize() <= 0 || file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new DomainException("El archivo de evidencia no debe superar 10 MB.");
        }

        String sanitizedOriginalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extensionFrom(sanitizedOriginalName).toLowerCase(Locale.ROOT);
        Set<String> allowedExtensions = EXTENSIONS_BY_FORMAT.getOrDefault(expectedFormatCode, Set.of());
        if (!allowedExtensions.contains(extension)) {
            throw new DomainException("La extension del archivo no corresponde al formato esperado de la evidencia.");
        }

        String mimeType = normalizeMimeType(file.getContentType());
        Set<String> allowedMimeTypes = MIME_TYPES_BY_FORMAT.getOrDefault(expectedFormatCode, Set.of());
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new DomainException("El tipo de archivo no corresponde al formato esperado de la evidencia.");
        }
    }

    private ResumenEvidenciaResponse mapSummary(GdrEvidence evidence) {
        Optional<GdrEvidenceReview> latestReview = evidenceReviewRepository.findLatestByEvidenceId(evidence.getId());
        return new ResumenEvidenciaResponse(
                evidence.getId(),
                evidence.getGoal().getId(),
                evidence.getTitle(),
                evidence.getDetail(),
                evidence.getEvidenceTypeCode(),
                resolveEvidenceTypeName(evidence.getEvidenceTypeCode()),
                evidence.getExpectedFormatCode(),
                resolveExpectedFormatName(evidence.getExpectedFormatCode()),
                evidence.getExpectedDate(),
                evidence.getFileOriginalName(),
                evidence.getFileSizeBytes(),
                evidence.getFileKey() != null && !evidence.getFileKey().isBlank(),
                evidence.getEvidenceStatus().getStatusCode(),
                evidence.getEvidenceStatus().getStatusName(),
                latestReview.map(GdrEvidenceReview::getReviewComment).orElse(null),
                latestReview.map(GdrEvidenceReview::getQualificationCode).orElse(null),
                latestReview
                        .map(GdrEvidenceReview::getQualificationCode)
                        .map(this::resolveQualificationName)
                        .orElse(null)
        );
    }

    private DetalleEvidenciaResponse mapDetail(GdrEvidence evidence) {
        List<RevisionEvidenciaResponse> reviews = evidenceReviewRepository.findByEvidenceId(evidence.getId()).stream()
                .map(this::mapReview)
                .toList();
        Optional<GdrCorrectiveAction> openAction = correctiveActionRepository.findOpenByEvidenceId(evidence.getId()).stream()
                .findFirst();

        return new DetalleEvidenciaResponse(
                evidence.getId(),
                evidence.getGoal().getId(),
                evidence.getGoal().getTitle(),
                evidence.getGoal().getAssignment().getEvaluatedPerson().getDisplayName(),
                evidence.getGoal().getIndicator().getName(),
                evidence.getTitle(),
                evidence.getDetail(),
                evidence.getEvidenceTypeCode(),
                resolveEvidenceTypeName(evidence.getEvidenceTypeCode()),
                evidence.getExpectedFormatCode(),
                resolveExpectedFormatName(evidence.getExpectedFormatCode()),
                evidence.getExpectedDate(),
                evidence.getFileOriginalName(),
                evidence.getFileMimeType(),
                evidence.getFileSizeBytes(),
                evidence.getFileKey() != null && !evidence.getFileKey().isBlank(),
                evidence.getEvidenceStatus().getStatusCode(),
                evidence.getEvidenceStatus().getStatusName(),
                openAction.map(GdrCorrectiveAction::getActionDetail).orElse(null),
                openAction.map(GdrCorrectiveAction::getActionStatus).orElse(null),
                reviews
        );
    }

    private RevisionEvidenciaResponse mapReview(GdrEvidenceReview review) {
        Optional<GdrCorrectiveAction> action = correctiveActionRepository.findByReviewId(review.getId());
        return new RevisionEvidenciaResponse(
                review.getId(),
                review.getEvidenceStatus().getStatusCode(),
                review.getEvidenceStatus().getStatusName(),
                review.getQualificationCode(),
                resolveQualificationName(review.getQualificationCode()),
                review.getReviewComment(),
                action.map(GdrCorrectiveAction::getActionDetail).orElse(null),
                action.map(GdrCorrectiveAction::getActionStatus).orElse(null),
                review.getReviewedAt()
        );
    }

    private void validateQualificationForEvidenceType(String qualificationCode, String evidenceTypeCode) {
        if (!QUALIFICATION_LABELS.containsKey(qualificationCode)) {
            throw new DomainException("La calificacion indicada no es valida.");
        }
        if ("AVANCE".equalsIgnoreCase(evidenceTypeCode)) {
            if (!ADVANCE_QUALIFICATIONS.contains(qualificationCode)) {
                throw new DomainException("La calificacion no corresponde a una evidencia de avance.");
            }
            return;
        }
        if ("FINAL".equalsIgnoreCase(evidenceTypeCode)) {
            if (!FINAL_QUALIFICATIONS.contains(qualificationCode)) {
                throw new DomainException("La calificacion no corresponde a una evidencia final.");
            }
            return;
        }
        throw new DomainException("El tipo de evidencia no admite calificacion.");
    }

    private String resolveDecisionFromQualification(String qualificationCode) {
        if (APPROVED_QUALIFICATIONS.contains(qualificationCode)) {
            return STATUS_APPROVED;
        }
        return STATUS_OBSERVED;
    }

    private String resolveQualificationName(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return QUALIFICATION_LABELS.getOrDefault(code.toUpperCase(Locale.ROOT), code);
    }

    private void closeOpenCorrectiveActions(Long evidenceId) {
        List<GdrCorrectiveAction> openActions = correctiveActionRepository.findOpenByEvidenceId(evidenceId);
        if (openActions.isEmpty()) {
            return;
        }

        LocalDateTime completedAt = LocalDateTime.now();
        openActions.forEach((action) -> {
            action.setActionStatus("COMPLETED");
            action.setCompletedAt(completedAt);
            correctiveActionRepository.save(action);
        });
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new DomainException(message);
        }
        return normalized;
    }

    private String normalizeCatalogCode(
            String value,
            Map<String, String> allowedValues,
            String requiredMessage,
            String invalidMessage
    ) {
        String normalized = normalizeRequired(value, requiredMessage).toUpperCase();
        if (!allowedValues.containsKey(normalized)) {
            throw new DomainException(invalidMessage);
        }
        return normalized;
    }

    private String resolveEvidenceTypeName(String code) {
        return EVIDENCE_TYPE_NAMES.getOrDefault(code, code);
    }

    private String resolveExpectedFormatName(String code) {
        return EXPECTED_FORMAT_NAMES.getOrDefault(code, code);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitizeOriginalName(String originalName) {
        String candidate = originalName == null ? "evidencia.bin" : originalName;
        candidate = candidate.replace('\\', '/');
        if (candidate.contains("/")) {
            candidate = candidate.substring(candidate.lastIndexOf('/') + 1);
        }
        candidate = candidate.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (candidate.isBlank()) {
            return "evidencia.bin";
        }
        return candidate;
    }

    private String extensionFrom(String originalName) {
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalName.length() - 1) {
            throw new DomainException("El archivo de evidencia debe tener una extension valida.");
        }
        return originalName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase(Locale.ROOT);
    }
}
