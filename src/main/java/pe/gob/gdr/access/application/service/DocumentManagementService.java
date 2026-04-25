package pe.gob.gdr.access.application.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.gdr.access.application.dto.request.CrearSolicitudFirmaRequest;
import pe.gob.gdr.access.application.dto.request.PrepararDocumentoFirmaRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarRetornoFirmaRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarDocumentoFirmadoRequest;
import pe.gob.gdr.access.application.dto.response.DocumentoFirmadoDetalleResponse;
import pe.gob.gdr.access.application.dto.response.DocumentoFirmadoResumenResponse;
import pe.gob.gdr.access.application.dto.response.InicioFirmaResponse;
import pe.gob.gdr.access.application.dto.response.PlantillaDocumentoResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudFirmaDetalleResponse;
import pe.gob.gdr.access.application.dto.response.TipoDocumentoResponse;
import pe.gob.gdr.access.application.port.DigitalSignaturePort;
import pe.gob.gdr.access.application.port.DocumentStoragePort;
import pe.gob.gdr.access.application.port.SignatureIntegrationStatus;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.DocFlowStatus;
import pe.gob.gdr.access.domain.model.DocHash;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.DocSignatureRequest;
import pe.gob.gdr.access.domain.model.DocTemplate;
import pe.gob.gdr.access.domain.model.DocType;
import pe.gob.gdr.access.domain.model.DocVersion;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.repository.DocFlowStatusRepository;
import pe.gob.gdr.access.domain.repository.DocHashRepository;
import pe.gob.gdr.access.domain.repository.DocSignedFileRepository;
import pe.gob.gdr.access.domain.repository.DocSignatureRequestRepository;
import pe.gob.gdr.access.domain.repository.DocTemplateRepository;
import pe.gob.gdr.access.domain.repository.DocTypeRepository;
import pe.gob.gdr.access.domain.repository.DocVersionRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;
import pe.gob.gdr.access.infrastructure.config.DocumentStorageProperties;

@Service
public class DocumentManagementService {

    private static final String ACTIVE_STATUS = "ACTIVO";
    private static final Set<String> PDF_EXTENSIONS = Set.of(".pdf");
    private static final String FLOW_LISTO_PARA_FIRMA = "LISTO_PARA_FIRMA";
    private static final String FLOW_FIRMA_SOLICITADA = "FIRMA_SOLICITADA";
    private static final String FLOW_EN_FIRMA = "EN_FIRMA";
    private static final String FLOW_FIRMADO = "FIRMADO";
    private static final String FLOW_REGISTRADO = "REGISTRADO";
    private static final String FLOW_VIGENTE = "VIGENTE";
    private static final String FLOW_ERROR_FIRMA = "ERROR_FIRMA";
    private static final String FLOW_FIRMA_CANCELADA = "FIRMA_CANCELADA";
    private static final String ACTIVE_FLOW_INDICATOR = "S";
    private static final String CLOSED_FLOW_INDICATOR = "N";
    private static final int PDF_FONT_SIZE = 12;
    private static final int PDF_TOP_Y = 760;
    private static final int PDF_LEFT_X = 48;
    private static final int PDF_LINE_HEIGHT = 18;
    private static final DateTimeFormatter PREPARED_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] DEFAULT_TEMPLATE_PDF = Base64.getDecoder().decode(
            "JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoKPDwgL1R5cGUg"
                    + "L1BhZ2VzIC9Db3VudCAxIC9LaWRzIFszIDAgUl0gPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUgL1BhZ2UgL1BhcmVudCAyIDAg"
                    + "UiAvTWVkaWFCb3ggWzAgMCA2MTIgNzkyXSAvQ29udGVudHMgNCAwIFIgL1Jlc291cmNlcyA8PCAvRm9udCA8PCAvRjEgNSAwIFIg"
                    + "Pj4gPj4gPj4KZW5kb2JqCjQgMCBvYmoKPDwgL0xlbmd0aCA5NyA+PgpzdHJlYW0KQlQKL0YxIDE4IFRmCjcyIDcyMCBUZAooUGxh"
                    + "bnRpbGxhIGluc3RpdHVjaW9uYWwgR0RSIEFjY2VzcykgVGoKMCAtMjggVGQKKFVzbyBpbnRlcm5vIGRlbCBwcm9jZXNvIEdEUikg"
                    + "VGoKRVQKZW5kc3RyZWFtCmVuZG9iago1IDAgb2JqCjw8IC9UeXBlIC9Gb250IC9TdWJ0eXBlIC9UeXBlMSAvQmFzZUZvbnQgL0hl"
                    + "bHZldGljYSA+PgplbmRvYmoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAw"
                    + "NTggMDAwMDAgbiAKMDAwMDAwMDExNSAwMDAwMCBuIAowMDAwMDAwMjQxIDAwMDAwIG4gCjAwMDAwMDAzOTggMDAwMDAgbiAKdHJh"
                    + "aWxlcgo8PCAvU2l6ZSA2IC9Sb290IDEgMCBSID4+CnN0YXJ0eHJlZgo0NjgKJSVFT0YK"
    );

    private final DocTypeRepository docTypeRepository;
    private final DocTemplateRepository docTemplateRepository;
    private final DocSignedFileRepository docSignedFileRepository;
    private final DocVersionRepository docVersionRepository;
    private final DocHashRepository docHashRepository;
    private final DocFlowStatusRepository docFlowStatusRepository;
    private final DocSignatureRequestRepository docSignatureRequestRepository;
    private final GdrResultRepository resultRepository;
    private final DigitalSignaturePort digitalSignaturePort;
    private final DocumentStoragePort documentStoragePort;
    private final DocumentStorageProperties storageProperties;
    private final NotificacionesService notificacionesService;

    public DocumentManagementService(
            DocTypeRepository docTypeRepository,
            DocTemplateRepository docTemplateRepository,
            DocSignedFileRepository docSignedFileRepository,
            DocVersionRepository docVersionRepository,
            DocHashRepository docHashRepository,
            DocFlowStatusRepository docFlowStatusRepository,
            DocSignatureRequestRepository docSignatureRequestRepository,
            GdrResultRepository resultRepository,
            DigitalSignaturePort digitalSignaturePort,
            DocumentStoragePort documentStoragePort,
            DocumentStorageProperties storageProperties,
            NotificacionesService notificacionesService
    ) {
        this.docTypeRepository = docTypeRepository;
        this.docTemplateRepository = docTemplateRepository;
        this.docSignedFileRepository = docSignedFileRepository;
        this.docVersionRepository = docVersionRepository;
        this.docHashRepository = docHashRepository;
        this.docFlowStatusRepository = docFlowStatusRepository;
        this.docSignatureRequestRepository = docSignatureRequestRepository;
        this.resultRepository = resultRepository;
        this.digitalSignaturePort = digitalSignaturePort;
        this.documentStoragePort = documentStoragePort;
        this.storageProperties = storageProperties;
        this.notificacionesService = notificacionesService;
    }

    public List<TipoDocumentoResponse> listDocumentTypes() {
        return docTypeRepository.findActiveTypes().stream()
                .map((type) -> new TipoDocumentoResponse(type.getId(), type.getCode(), type.getName(), type.getDescription()))
                .toList();
    }

    public List<PlantillaDocumentoResponse> listTemplates() {
        return docTemplateRepository.findActiveTemplates().stream()
                .map(this::mapTemplateResponse)
                .toList();
    }

    public List<DocumentoFirmadoResumenResponse> listSignedDocuments(Long evaluatedId) {
        return docSignedFileRepository.findActiveByEvaluatedIdInActiveCycle(evaluatedId).stream()
                .map(this::mapSummaryResponse)
                .toList();
    }

    public DocumentoFirmadoDetalleResponse getSignedDocument(Long documentId) {
        DocSignedFile document = docSignedFileRepository.findActiveById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el documento firmado solicitado."));
        return mapDetailResponse(document);
    }

    public ResponseEntity<Resource> downloadTemplate(Long templateId) {
        DocTemplate template = docTemplateRepository.findActiveById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la plantilla solicitada."));
        ensureTemplateAvailable(template);
        Resource resource = documentStoragePort.loadAsResource(template.getFileKey());
        return buildFileResponse(resource, template.getMimeType(), template.getOriginalName(), true);
    }

    public ResponseEntity<Resource> previewSignedDocument(Long documentId, boolean download) {
        DocSignedFile document = docSignedFileRepository.findActiveById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el documento firmado solicitado."));
        Resource resource = documentStoragePort.loadAsResource(document.getFileKey());
        return buildFileResponse(resource, document.getMimeType(), document.getOriginalName(), download);
    }

    public ResponseEntity<Resource> previewPreparedDocument(Long requestId, boolean download) {
        DocSignatureRequest request = findSignatureRequest(requestId);
        Resource resource = documentStoragePort.loadAsResource(request.getPreparedFileKey());
        return buildFileResponse(resource, request.getPreparedMimeType(), request.getPreparedOriginalName(), download);
    }

    @Transactional
    public SolicitudFirmaDetalleResponse prepareDocumentForSignature(
            PrepararDocumentoFirmaRequest request,
            String username
    ) {
        DocTemplate template = docTemplateRepository.findActiveById(request.plantillaId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la plantilla solicitada."));
        GdrResult result = findResultByEvaluatedId(request.evaluatedId());
        validateNoActiveSignedDocument(result.getId(), template.getDocType().getId());

        DocSignatureRequest reusableRequest = findReusableSignatureRequest(result.getId(), template.getDocType().getId());
        if (reusableRequest != null) {
            return mapSignatureResponse(refreshPreparedDocument(reusableRequest, template, result, username));
        }

        byte[] preparedContent = buildPreparedDocumentContent(template, result);
        String preparedFileKey = documentStoragePort.store("documentos-preparados", ".pdf", preparedContent);
        String preparedFileName = buildPreparedFileName(template, result);

        DocSignatureRequest signatureRequest = DocSignatureRequest.builder()
                .result(result)
                .docType(template.getDocType())
                .template(template)
                .flowStatus(findFlowStatus(FLOW_LISTO_PARA_FIRMA))
                .signatureProvider(digitalSignaturePort.getProviderCode())
                .preparedFileKey(preparedFileKey)
                .preparedOriginalName(preparedFileName)
                .preparedMimeType("application/pdf")
                .preparedHash(calculateSha256(preparedContent))
                .requestUser(username)
                .signatureResultMessage(buildPreparationMessage())
                .activeFlowIndicator(ACTIVE_FLOW_INDICATOR)
                .build();

        return mapSignatureResponse(docSignatureRequestRepository.save(signatureRequest));
    }

    private DocSignatureRequest refreshPreparedDocument(
            DocSignatureRequest signatureRequest,
            DocTemplate template,
            GdrResult result,
            String username
    ) {
        byte[] preparedContent = buildPreparedDocumentContent(template, result);
        String preparedFileKey = documentStoragePort.store("documentos-preparados", ".pdf", preparedContent);
        String preparedFileName = buildPreparedFileName(template, result);

        signatureRequest.setTemplate(template);
        signatureRequest.setPreparedFileKey(preparedFileKey);
        signatureRequest.setPreparedOriginalName(preparedFileName);
        signatureRequest.setPreparedMimeType("application/pdf");
        signatureRequest.setPreparedHash(calculateSha256(preparedContent));
        signatureRequest.setRequestUser(username);
        signatureRequest.setSignatureResultMessage(buildPreparationMessage());
        return docSignatureRequestRepository.save(signatureRequest);
    }

    @Transactional
    public SolicitudFirmaDetalleResponse createSignatureRequest(
            CrearSolicitudFirmaRequest request,
            String username
    ) {
        DocSignatureRequest signatureRequest = findSignatureRequest(request.solicitudFirmaId());
        String currentFlowCode = signatureRequest.getFlowStatus().getCode();
        if (FLOW_FIRMA_SOLICITADA.equalsIgnoreCase(currentFlowCode)) {
            return mapSignatureResponse(signatureRequest);
        }
        if (!FLOW_LISTO_PARA_FIRMA.equalsIgnoreCase(currentFlowCode)) {
            throw new DomainException("La solicitud de firma no se encuentra lista para ser iniciada.");
        }

        validateNoActiveSignedDocument(signatureRequest.getResult().getId(), signatureRequest.getDocType().getId());
        signatureRequest.setFlowStatus(findFlowStatus(FLOW_FIRMA_SOLICITADA));
        signatureRequest.setRequestUser(username);
        signatureRequest.setRequestDate(LocalDateTime.now());
        signatureRequest.setSignatureResultCode(null);
        signatureRequest.setSignatureResultMessage(buildPreparationMessage());
        signatureRequest.setActiveFlowIndicator(ACTIVE_FLOW_INDICATOR);

        return mapSignatureResponse(docSignatureRequestRepository.save(signatureRequest));
    }

    @Transactional
    public InicioFirmaResponse startSignatureRequest(Long requestId) {
        DocSignatureRequest signatureRequest = findSignatureRequest(requestId);
        String currentFlowCode = signatureRequest.getFlowStatus().getCode();
        SignatureIntegrationStatus integrationStatus = digitalSignaturePort.getIntegrationStatus();
        if (FLOW_EN_FIRMA.equalsIgnoreCase(currentFlowCode) && signatureRequest.getExternalTransactionId() != null) {
            return buildStartResponse(
                    signatureRequest,
                    safeResolveLaunchUrl(signatureRequest.getExternalTransactionId()),
                    "La solicitud ya se encuentra en firma. El retorno automatico dependera del convenio institucional disponible.",
                    integrationStatus
            );
        }
        if (!FLOW_FIRMA_SOLICITADA.equalsIgnoreCase(currentFlowCode)) {
            throw new DomainException("La solicitud de firma debe estar creada antes de iniciar el proceso.");
        }

        validateNoActiveSignedDocument(signatureRequest.getResult().getId(), signatureRequest.getDocType().getId());
        var signatureStart = digitalSignaturePort.startSignature(signatureRequest);
        signatureRequest.setFlowStatus(findFlowStatus(FLOW_EN_FIRMA));
        signatureRequest.setExternalTransactionId(signatureStart.externalTransactionId());
        signatureRequest.setSignatureStartDate(LocalDateTime.now());
        signatureRequest.setSignatureResultMessage(signatureStart.message());
        signatureRequest.setActiveFlowIndicator(ACTIVE_FLOW_INDICATOR);
        signatureRequest = docSignatureRequestRepository.save(signatureRequest);

        return buildStartResponse(signatureRequest, signatureStart.launchUrl(), signatureStart.message(), integrationStatus);
    }

    @Transactional
    public SolicitudFirmaDetalleResponse getSignatureRequest(Long requestId) {
        DocSignatureRequest request = findSignatureRequest(requestId);
        return mapSignatureResponse(advanceRequestToVigenteIfApplicable(request));
    }

    @Transactional
    public SolicitudFirmaDetalleResponse registerSignatureReturn(
            Long requestId,
            RegistrarRetornoFirmaRequest request,
            MultipartFile signedFile,
            String username
    ) {
        DocSignatureRequest signatureRequest = findSignatureRequest(requestId);
        String targetFlowCode = normalizeFlowCode(request.codigoEstadoFlujo());
        signatureRequest.setReturnDate(LocalDateTime.now());
        signatureRequest.setSignatureResultCode(normalizeResultCode(request.codigoResultadoFirma()));
        signatureRequest.setSignatureResultMessage(normalizeResultMessage(request.mensajeResultadoFirma()));

        if (FLOW_FIRMA_CANCELADA.equals(targetFlowCode)) {
            signatureRequest.setFlowStatus(findFlowStatus(FLOW_FIRMA_CANCELADA));
            signatureRequest.setActiveFlowIndicator(CLOSED_FLOW_INDICATOR);
            if (signatureRequest.getSignatureResultMessage() == null || signatureRequest.getSignatureResultMessage().isBlank()) {
                signatureRequest.setSignatureResultMessage("La firma fue cancelada antes del registro documental.");
            }
            return mapSignatureResponse(docSignatureRequestRepository.save(signatureRequest));
        }
        if (FLOW_ERROR_FIRMA.equals(targetFlowCode)) {
            signatureRequest.setFlowStatus(findFlowStatus(FLOW_ERROR_FIRMA));
            signatureRequest.setActiveFlowIndicator(CLOSED_FLOW_INDICATOR);
            if (signatureRequest.getSignatureResultMessage() == null || signatureRequest.getSignatureResultMessage().isBlank()) {
                signatureRequest.setSignatureResultMessage("La firma no pudo completarse y la solicitud quedo registrada con error.");
            }
            return mapSignatureResponse(docSignatureRequestRepository.save(signatureRequest));
        }
        if (!FLOW_FIRMADO.equals(targetFlowCode)) {
            throw new DomainException("El retorno de firma solo admite FIRMADO, ERROR_FIRMA o FIRMA_CANCELADA.");
        }
        if (signedFile == null || signedFile.isEmpty()) {
            throw new DomainException("Debe adjuntar el documento firmado al registrar un retorno exitoso.");
        }

        validateUploadedFile(signedFile);
        validateNoActiveSignedDocument(signatureRequest.getResult().getId(), signatureRequest.getDocType().getId());

        try {
            byte[] content = signedFile.getBytes();
            signatureRequest.setFlowStatus(findFlowStatus(FLOW_FIRMADO));
            DocSignedFile storedDocument = persistSignedDocument(
                    signatureRequest.getResult(),
                    signatureRequest.getDocType(),
                    content,
                    sanitizeOriginalName(
                            signedFile.getOriginalFilename() == null || signedFile.getOriginalFilename().isBlank()
                                    ? signatureRequest.getPreparedOriginalName()
                                    : signedFile.getOriginalFilename()
                    ),
                    normalizeMimeType(signedFile.getContentType()),
                    username
            );
            signatureRequest.setSignedDocument(storedDocument);
            signatureRequest.setDocumentRegisteredAt(LocalDateTime.now());
            signatureRequest.setFlowStatus(findFlowStatus(FLOW_REGISTRADO));
            signatureRequest.setActiveFlowIndicator(CLOSED_FLOW_INDICATOR);
            if (signatureRequest.getSignatureResultMessage() == null || signatureRequest.getSignatureResultMessage().isBlank()) {
                signatureRequest.setSignatureResultMessage(
                            "Documento firmado recibido y registrado. La vigencia documental se confirmara en la siguiente consulta del sistema."
                );
            }
            notificacionesService.emitForUser(
                    username,
                    NotificacionesService.DOCUMENTO_FIRMADO_REGISTRADO,
                    "DOC-" + storedDocument.getId()
            );
            return mapSignatureResponse(docSignatureRequestRepository.save(signatureRequest));
        } catch (IOException exception) {
            throw new DomainException("No se pudo procesar el archivo firmado retornado por la plataforma.");
        }
    }

    @Transactional
    public DocumentoFirmadoDetalleResponse registerSignedDocument(
            RegistrarDocumentoFirmadoRequest request,
            MultipartFile archivo,
            String username
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new DomainException("Debe adjuntar el documento firmado.");
        }

        DocType docType = docTypeRepository.findActiveById(request.tipoDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el tipo documental solicitado."));
        GdrResult result = findResultByEvaluatedId(request.evaluatedId());
        validateNoActiveSignedDocument(result.getId(), docType.getId());

        validateUploadedFile(archivo);

        try {
            byte[] content = archivo.getBytes();
            DocSignedFile signedFile = persistSignedDocument(
                    result,
                    docType,
                    content,
                    sanitizeOriginalName(archivo.getOriginalFilename()),
                    normalizeMimeType(archivo.getContentType()),
                    username
            );
            notificacionesService.emitForUser(
                    username,
                    NotificacionesService.DOCUMENTO_FIRMADO_REGISTRADO,
                    "DOC-" + signedFile.getId()
            );
            return mapDetailResponse(signedFile);
        } catch (Exception exception) {
            if (exception instanceof DomainException domainException) {
                throw domainException;
            }
            throw new DomainException("No se pudo registrar el documento firmado.");
        }
    }

    private void validateUploadedFile(MultipartFile file) {
        String mimeType = normalizeMimeType(file.getContentType());
        List<String> allowedMimeTypes = storageProperties.getAllowedMimeTypes().stream()
                .map(this::normalizeMimeType)
                .toList();

        if (!allowedMimeTypes.contains(mimeType)) {
            throw new DomainException("El documento firmado debe cargarse en un formato permitido por el sistema.");
        }
        if (file.getSize() <= 0 || file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new DomainException("El documento firmado excede el tamano maximo permitido.");
        }
        String sanitizedOriginalName = sanitizeOriginalName(file.getOriginalFilename());
        if (PDF_EXTENSIONS.stream().noneMatch((extension) -> sanitizedOriginalName.toLowerCase(Locale.ROOT).endsWith(extension))) {
            throw new DomainException("El documento firmado debe tener extension PDF.");
        }
    }

    private String sanitizeOriginalName(String originalName) {
        String candidate = originalName == null ? "documento_firmado.pdf" : originalName;
        candidate = candidate.replace('\\', '/');
        if (candidate.contains("/")) {
            candidate = candidate.substring(candidate.lastIndexOf('/') + 1);
        }
        candidate = candidate.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (candidate.isBlank()) {
            return "documento_firmado.pdf";
        }
        return candidate;
    }

    private String calculateSha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new DomainException("No se pudo calcular el hash del documento firmado.");
        }
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFlowCode(String flowCode) {
        return flowCode == null ? "" : flowCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeResultCode(String resultCode) {
        if (resultCode == null || resultCode.isBlank()) {
            return null;
        }
        return resultCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeResultMessage(String resultMessage) {
        if (resultMessage == null || resultMessage.isBlank()) {
            return null;
        }
        return resultMessage.trim();
    }

    private PlantillaDocumentoResponse mapTemplateResponse(DocTemplate template) {
        long templateSize = resolveTemplateSize(template);
        return new PlantillaDocumentoResponse(
                template.getId(),
                template.getDocType().getId(),
                template.getDocType().getCode(),
                template.getDocType().getName(),
                template.getTemplateName(),
                template.getDescription(),
                template.getOriginalName(),
                template.getMimeType(),
                templateSize
        );
    }

    private DocumentoFirmadoResumenResponse mapSummaryResponse(DocSignedFile document) {
        return new DocumentoFirmadoResumenResponse(
                document.getId(),
                document.getResult().getId(),
                document.getResult().getAssignment().getEvaluatedPerson().getId(),
                document.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                document.getDocType().getId(),
                document.getDocType().getName(),
                document.getOriginalName(),
                document.getMimeType(),
                document.getSizeBytes(),
                document.getCurrentVersion(),
                document.getStatus(),
                document.getUploadUser(),
                document.getUploadDate()
        );
    }

    private DocumentoFirmadoDetalleResponse mapDetailResponse(DocSignedFile document) {
        return new DocumentoFirmadoDetalleResponse(
                document.getId(),
                document.getResult().getId(),
                document.getResult().getAssignment().getEvaluatedPerson().getId(),
                document.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                document.getResult().getAssignment().getEvaluatorPerson().getDisplayName(),
                document.getResult().getAssignment().getCycle().getName(),
                document.getDocType().getId(),
                document.getDocType().getCode(),
                document.getDocType().getName(),
                document.getOriginalName(),
                document.getMimeType(),
                document.getSizeBytes(),
                document.getCurrentVersion(),
                document.getStatus(),
                document.getUploadUser(),
                document.getUploadDate()
        );
    }

    private SolicitudFirmaDetalleResponse mapSignatureResponse(DocSignatureRequest request) {
        SignatureIntegrationStatus integrationStatus = digitalSignaturePort.getIntegrationStatus();
        return new SolicitudFirmaDetalleResponse(
                request.getId(),
                request.getResult().getId(),
                request.getResult().getAssignment().getEvaluatedPerson().getId(),
                request.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                request.getResult().getAssignment().getEvaluatorPerson().getDisplayName(),
                request.getResult().getAssignment().getCycle().getName(),
                request.getDocType().getId(),
                request.getDocType().getCode(),
                request.getDocType().getName(),
                request.getTemplate().getId(),
                request.getTemplate().getTemplateName(),
                request.getPreparedOriginalName(),
                request.getFlowStatus().getCode(),
                request.getSignatureProvider(),
                request.getExternalTransactionId(),
                safeResolveLaunchUrl(request.getExternalTransactionId()),
                request.getRequestDate(),
                request.getSignatureStartDate(),
                request.getReturnDate(),
                request.getDocumentRegisteredAt(),
                request.getSignatureResultCode(),
                request.getSignatureResultMessage(),
                request.getSignedDocument() != null ? request.getSignedDocument().getId() : null,
                canStartSignature(request),
                canRegisterReturn(request),
                request.getPreparedFileKey() != null && !request.getPreparedFileKey().isBlank(),
                request.getSignedDocument() != null,
                integrationStatus.officialIntegrationAvailable(),
                integrationStatus.providerStatusQueryAvailable(),
                integrationStatus.automaticReturnEnabled(),
                integrationStatus.integrationMode(),
                integrationStatus.integrationMessage()
        );
    }

    private InicioFirmaResponse buildStartResponse(
            DocSignatureRequest request,
            String launchUrl,
            String message,
            SignatureIntegrationStatus integrationStatus
    ) {
        return new InicioFirmaResponse(
                request.getId(),
                request.getFlowStatus().getCode(),
                request.getSignatureProvider(),
                request.getExternalTransactionId(),
                launchUrl,
                message,
                integrationStatus.officialIntegrationAvailable(),
                integrationStatus.providerStatusQueryAvailable(),
                integrationStatus.automaticReturnEnabled(),
                integrationStatus.integrationMode(),
                integrationStatus.integrationMessage()
        );
    }

    private ResponseEntity<Resource> buildFileResponse(
            Resource resource,
            String mimeType,
            String originalName,
            boolean download
    ) {
        ContentDisposition contentDisposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(originalName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .cacheControl(CacheControl.noCache())
                .header("Content-Disposition", contentDisposition.toString())
                .body(resource);
    }

    private long resolveTemplateSize(DocTemplate template) {
        try {
            ensureTemplateAvailable(template);
            return Files.size(resolveStoragePath(template.getFileKey()));
        } catch (IOException | DomainException exception) {
            return template.getSizeBytes();
        }
    }

    private void ensureTemplateAvailable(DocTemplate template) {
        Path templatePath = resolveStoragePath(template.getFileKey());
        if (Files.exists(templatePath) && Files.isRegularFile(templatePath)) {
            return;
        }

        try {
            Files.createDirectories(templatePath.getParent());
            Files.write(templatePath, DEFAULT_TEMPLATE_PDF);
        } catch (IOException exception) {
            throw new DomainException("No se pudo provisionar la plantilla documental solicitada.");
        }
    }

    private Path resolveStoragePath(String fileKey) {
        if (storageProperties.getStorageBasePath() == null || storageProperties.getStorageBasePath().isBlank()) {
            throw new DomainException("La ruta base de almacenamiento documental no esta configurada.");
        }

        Path basePath = Paths.get(storageProperties.getStorageBasePath()).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(fileKey).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw new DomainException("La clave interna de la plantilla no es valida.");
        }
        return resolvedPath;
    }

    private GdrResult findResultByEvaluatedId(Long evaluatedId) {
        return resultRepository.findByEvaluatedPersonIdInActiveCycle(evaluatedId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el resultado consolidado del evaluado."));
    }

    private void validateNoActiveSignedDocument(Long resultId, Long typeId) {
        docSignedFileRepository.findActiveByResultIdAndTypeId(resultId, typeId).ifPresent((existing) -> {
            throw new DomainException(
                    "Ya existe un documento firmado activo para el resultado y tipo documental seleccionados."
            );
        });
    }

    private DocFlowStatus findFlowStatus(String code) {
        return docFlowStatusRepository.findActiveByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el estado documental solicitado."));
    }

    private DocSignatureRequest findSignatureRequest(Long requestId) {
        return docSignatureRequestRepository.findActiveById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la solicitud de firma solicitada."));
    }

    private DocSignatureRequest findReusableSignatureRequest(Long resultId, Long typeId) {
        return docSignatureRequestRepository.findByResultIdAndTypeIdOrderByCreatedAtDesc(resultId, typeId).stream()
                .filter((request) -> ACTIVE_FLOW_INDICATOR.equalsIgnoreCase(request.getActiveFlowIndicator()))
                .filter((request) -> request.getSignedDocument() == null)
                .filter((request) -> {
                    String flowCode = normalizeFlowCode(request.getFlowStatus().getCode());
                    return FLOW_LISTO_PARA_FIRMA.equals(flowCode)
                            || FLOW_FIRMA_SOLICITADA.equals(flowCode)
                            || FLOW_EN_FIRMA.equals(flowCode);
                })
                .findFirst()
                .orElse(null);
    }

    private DocSignatureRequest advanceRequestToVigenteIfApplicable(DocSignatureRequest request) {
        if (!FLOW_REGISTRADO.equals(normalizeFlowCode(request.getFlowStatus().getCode()))) {
            return request;
        }
        if (request.getSignedDocument() == null) {
            return request;
        }

        request.setFlowStatus(findFlowStatus(FLOW_VIGENTE));
        if (request.getSignatureResultMessage() == null || request.getSignatureResultMessage().isBlank()) {
            request.setSignatureResultMessage("Documento registrado y disponible como vigente para consulta posterior.");
        }
        return docSignatureRequestRepository.save(request);
    }

    private byte[] buildPreparedDocumentContent(DocTemplate template, GdrResult result) {
        ensureTemplateAvailable(template);
        return buildPdfDocument(buildPreparedDocumentLines(template, result));
    }

    private String buildPreparedFileName(DocTemplate template, GdrResult result) {
        String originalName = sanitizeOriginalName(template.getOriginalName());
        String baseName = originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? originalName.substring(0, originalName.length() - 4)
                : originalName;
        return sanitizeOriginalName(baseName + "_resultado_" + result.getId() + "_para_firma.pdf");
    }

    private String buildPreparationMessage() {
        return "Documento preparado con datos reales del resultado GDR. La integracion oficial con Firma Peru sigue pendiente por convenio institucional.";
    }

    private boolean canStartSignature(DocSignatureRequest request) {
        String flowCode = normalizeFlowCode(request.getFlowStatus().getCode());
        return ACTIVE_FLOW_INDICATOR.equalsIgnoreCase(request.getActiveFlowIndicator())
                && request.getSignedDocument() == null
                && (FLOW_LISTO_PARA_FIRMA.equals(flowCode) || FLOW_FIRMA_SOLICITADA.equals(flowCode));
    }

    private boolean canRegisterReturn(DocSignatureRequest request) {
        String flowCode = normalizeFlowCode(request.getFlowStatus().getCode());
        return ACTIVE_FLOW_INDICATOR.equalsIgnoreCase(request.getActiveFlowIndicator())
                && request.getSignedDocument() == null
                && (FLOW_FIRMA_SOLICITADA.equals(flowCode) || FLOW_EN_FIRMA.equals(flowCode));
    }

    private String safeResolveLaunchUrl(String externalTransactionId) {
        try {
            return digitalSignaturePort.resolveLaunchUrl(externalTransactionId);
        } catch (DomainException exception) {
            return null;
        }
    }

    private List<String> buildPreparedDocumentLines(DocTemplate template, GdrResult result) {
        List<String> lines = new ArrayList<>();
        lines.add("GDR Access - Documento listo para firma");
        lines.add(resolvePreparedDocumentTitle(template));
        lines.add("Resultado consolidado ID: " + result.getId());
        lines.add("Evaluado: " + result.getAssignment().getEvaluatedPerson().getDisplayName());
        lines.add("Evaluador: " + result.getAssignment().getEvaluatorPerson().getDisplayName());
        lines.add("Ciclo: " + result.getAssignment().getCycle().getName());
        lines.add("Puntaje consolidado: " + formatScore(result.getConsolidatedScore()));
        lines.add("Plantilla base: " + template.getTemplateName());
        lines.add("Tipo documental: " + template.getDocType().getName());
        lines.add("Preparado el: " + LocalDateTime.now().format(PREPARED_DATE_FORMATTER));
        lines.add("Este archivo fue generado por el sistema con datos reales del resultado GDR.");

        if ("ACTA_RETROALIMENTACION".equalsIgnoreCase(template.getDocType().getCode())) {
            lines.add("Acta base para la retroalimentacion institucional del evaluado.");
            lines.add("Espacio reservado para la firma del responsable y del participante.");
        } else {
            lines.add("Formato base para la formalizacion del resultado consolidado.");
            lines.add("Espacio reservado para la firma del documento aprobado.");
        }

        lines.add("La integracion oficial con Firma Peru depende del convenio institucional del entorno.");
        return wrapLines(lines, 88);
    }

    private String resolvePreparedDocumentTitle(DocTemplate template) {
        if ("ACTA_RETROALIMENTACION".equalsIgnoreCase(template.getDocType().getCode())) {
            return "Acta de retroalimentacion lista para firma";
        }
        return "Formato de resultado listo para firma";
    }

    private String formatScore(BigDecimal score) {
        if (score == null) {
            return "0.0000";
        }
        return score.stripTrailingZeros().toPlainString();
    }

    private List<String> wrapLines(List<String> sourceLines, int maxLength) {
        List<String> wrappedLines = new ArrayList<>();
        for (String line : sourceLines) {
            String current = line == null ? "" : line.trim();
            while (current.length() > maxLength) {
                int splitIndex = current.lastIndexOf(' ', maxLength);
                if (splitIndex <= 0) {
                    splitIndex = maxLength;
                }
                wrappedLines.add(current.substring(0, splitIndex).trim());
                current = current.substring(splitIndex).trim();
            }
            wrappedLines.add(current);
        }
        return wrappedLines;
    }

    private byte[] buildPdfDocument(List<String> lines) {
        List<String> safeLines = lines.isEmpty() ? List.of("Documento GDR listo para firma.") : lines;
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("BT\n");
        contentBuilder.append("/F1 ").append(PDF_FONT_SIZE).append(" Tf\n");
        contentBuilder.append(PDF_LEFT_X).append(" ").append(PDF_TOP_Y).append(" Td\n");

        for (int index = 0; index < safeLines.size(); index++) {
            String escaped = escapePdfText(safeLines.get(index));
            contentBuilder.append("(").append(escaped).append(") Tj\n");
            if (index < safeLines.size() - 1) {
                contentBuilder.append("0 -").append(PDF_LINE_HEIGHT).append(" Td\n");
            }
        }
        contentBuilder.append("ET\n");

        byte[] streamBytes = contentBuilder.toString().getBytes(StandardCharsets.US_ASCII);
        String object1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String object2 = "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n";
        String object3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R "
                + "/Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n";
        String object4Header = "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
        String object4Footer = "endstream\nendobj\n";
        String object5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n";

        List<byte[]> objects = List.of(
                object1.getBytes(StandardCharsets.US_ASCII),
                object2.getBytes(StandardCharsets.US_ASCII),
                object3.getBytes(StandardCharsets.US_ASCII),
                object4Header.getBytes(StandardCharsets.US_ASCII),
                streamBytes,
                object4Footer.getBytes(StandardCharsets.US_ASCII),
                object5.getBytes(StandardCharsets.US_ASCII)
        );

        String pdfHeader = "%PDF-1.4\n";
        List<Integer> offsets = new ArrayList<>();
        int currentOffset = pdfHeader.getBytes(StandardCharsets.US_ASCII).length;

        offsets.add(0);
        offsets.add(currentOffset);
        currentOffset += objects.get(0).length;
        offsets.add(currentOffset);
        currentOffset += objects.get(1).length;
        offsets.add(currentOffset);
        currentOffset += objects.get(2).length;
        offsets.add(currentOffset);
        currentOffset += objects.get(3).length + objects.get(4).length + objects.get(5).length;
        offsets.add(currentOffset);
        currentOffset += objects.get(6).length;

        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 6\n");
        xref.append(String.format(Locale.ROOT, "%010d 65535 f \n", 0));
        for (int index = 1; index <= 5; index++) {
            xref.append(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(index)));
        }

        String trailer = "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n"
                + currentOffset
                + "\n%%EOF";

        byte[] xrefBytes = xref.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] trailerBytes = trailer.getBytes(StandardCharsets.US_ASCII);
        byte[] pdfBytes = new byte[currentOffset + xrefBytes.length + trailerBytes.length];
        int cursor = 0;
        byte[] headerBytes = pdfHeader.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(headerBytes, 0, pdfBytes, cursor, headerBytes.length);
        cursor += headerBytes.length;

        for (byte[] object : objects) {
            System.arraycopy(object, 0, pdfBytes, cursor, object.length);
            cursor += object.length;
        }
        System.arraycopy(xrefBytes, 0, pdfBytes, cursor, xrefBytes.length);
        cursor += xrefBytes.length;
        System.arraycopy(trailerBytes, 0, pdfBytes, cursor, trailerBytes.length);
        return pdfBytes;
    }

    private String escapePdfText(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\n", " ");
    }

    private DocSignedFile persistSignedDocument(
            GdrResult result,
            DocType docType,
            byte[] content,
            String originalName,
            String mimeType,
            String username
    ) {
        String fileKey = documentStoragePort.store("documentos-firmados", ".pdf", content);
        String hashValue = calculateSha256(content);
        String normalizedMimeType = mimeType == null || mimeType.isBlank() ? "application/pdf" : mimeType;

        DocSignedFile signedFile = DocSignedFile.builder()
                .result(result)
                .docType(docType)
                .originalName(originalName)
                .mimeType(normalizedMimeType)
                .sizeBytes((long) content.length)
                .currentVersion(1)
                .fileKey(fileKey)
                .status(ACTIVE_STATUS)
                .uploadUser(username)
                .uploadDate(LocalDateTime.now())
                .build();
        signedFile = docSignedFileRepository.save(signedFile);

        DocVersion documentVersion = DocVersion.builder()
                .signedFile(signedFile)
                .versionNumber(1)
                .fileKey(fileKey)
                .sizeBytes((long) content.length)
                .registeredUser(username)
                .registeredAt(LocalDateTime.now())
                .status(ACTIVE_STATUS)
                .build();
        documentVersion = docVersionRepository.save(documentVersion);

        docHashRepository.save(DocHash.builder()
                .signedFile(signedFile)
                .documentVersion(documentVersion)
                .hashAlgorithm("SHA-256")
                .hashValue(hashValue)
                .build());

        return signedFile;
    }
}
