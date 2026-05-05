package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.gdr.access.application.dto.request.CrearSolicitudFirmaRequest;
import pe.gob.gdr.access.application.dto.request.PrepararDocumentoFirmaRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarRetornoFirmaRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarDocumentoFirmadoRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.DocumentoFirmadoDetalleResponse;
import pe.gob.gdr.access.application.dto.response.DocumentoFirmadoResumenResponse;
import pe.gob.gdr.access.application.dto.response.HrOrgUnitOrganigramaResponse;
import pe.gob.gdr.access.application.dto.response.InicioFirmaResponse;
import pe.gob.gdr.access.application.dto.response.PageResponse;
import pe.gob.gdr.access.application.dto.response.PlantillaDocumentoResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudFirmaDetalleResponse;
import pe.gob.gdr.access.application.dto.response.TipoDocumentoResponse;
import pe.gob.gdr.access.application.service.DocumentManagementService;
import pe.gob.gdr.access.application.service.HrOrgUnitCatalogService;

@RestController
@RequestMapping("/documentos")
public class DocumentManagementController {

    private static final int SIGNED_DOCUMENTS_MAX_PAGE_SIZE = 100;

    private final DocumentManagementService documentManagementService;
    private final HrOrgUnitCatalogService hrOrgUnitCatalogService;

    public DocumentManagementController(
            DocumentManagementService documentManagementService,
            HrOrgUnitCatalogService hrOrgUnitCatalogService
    ) {
        this.documentManagementService = documentManagementService;
        this.hrOrgUnitCatalogService = hrOrgUnitCatalogService;
    }

    @GetMapping("/tipos")
    @PreAuthorize("@gdrAccessPolicyService.canViewDocuments(authentication)")
    public ResponseEntity<ApiResponse<List<TipoDocumentoResponse>>> listDocumentTypes() {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.listDocumentTypes(),
                "Tipos documentales consultados correctamente."
        ));
    }

    @GetMapping("/plantillas")
    @PreAuthorize("@gdrAccessPolicyService.canViewDocuments(authentication)")
    public ResponseEntity<ApiResponse<List<PlantillaDocumentoResponse>>> listTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.listTemplates(),
                "Plantillas documentales consultadas correctamente."
        ));
    }

    @GetMapping("/plantillas/{templateId}/descarga")
    @PreAuthorize("@gdrAccessPolicyService.canViewDocuments(authentication)")
    public ResponseEntity<?> downloadTemplate(@PathVariable Long templateId) {
        return documentManagementService.downloadTemplate(templateId);
    }

    @GetMapping("/firmados")
    @PreAuthorize("@gdrAccessPolicyService.canAccessDocumentsForEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<ApiResponse<PageResponse<DocumentoFirmadoResumenResponse>>> listSignedDocuments(
            @RequestParam("evaluatedId") Long evaluatedId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), SIGNED_DOCUMENTS_MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<DocumentoFirmadoResumenResponse> result = documentManagementService.listSignedDocuments(evaluatedId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(result),
                "Documentos firmados consultados correctamente."
        ));
    }

    @GetMapping("/unidades-organizacionales")
    @PreAuthorize("@gdrAccessPolicyService.canViewDocuments(authentication)")
    public ResponseEntity<ApiResponse<List<HrOrgUnitOrganigramaResponse>>> listOrganizationalUnitsForSigning() {
        return ResponseEntity.ok(ApiResponse.ok(
                hrOrgUnitCatalogService.listOfficesForSigning(),
                "Oficinas consultadas correctamente."
        ));
    }

    @GetMapping("/formato-gdr/pdf")
    @PreAuthorize("@gdrAccessPolicyService.canPrepareDocumentsForEvaluated(authentication, #evaluatedId)")
    public ResponseEntity<?> downloadFormatoGdrPdf(@RequestParam("evaluatedId") Long evaluatedId) {
        return documentManagementService.downloadFormatoGdrPdf(evaluatedId);
    }

    @PostMapping("/firmas/preparar")
    @PreAuthorize("@gdrAccessPolicyService.canPrepareDocumentsForEvaluated(authentication, #request.evaluatedId())")
    public ResponseEntity<ApiResponse<SolicitudFirmaDetalleResponse>> prepareDocumentForSignature(
            @Valid @RequestBody PrepararDocumentoFirmaRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.prepareDocumentForSignature(request, principal.getName()),
                "Documento preparado para firma correctamente."
        ));
    }

    @PostMapping("/firmas/solicitudes")
    @PreAuthorize("@gdrAccessPolicyService.canStartSignatureRequest(authentication, #request.solicitudFirmaId())")
    public ResponseEntity<ApiResponse<SolicitudFirmaDetalleResponse>> createSignatureRequest(
            @Valid @RequestBody CrearSolicitudFirmaRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.createSignatureRequest(request, principal.getName()),
                "Solicitud de firma registrada correctamente."
        ));
    }

    @PostMapping("/firmas/solicitudes/{solicitudFirmaId}/iniciar")
    @PreAuthorize("@gdrAccessPolicyService.canStartSignatureRequest(authentication, #solicitudFirmaId)")
    public ResponseEntity<ApiResponse<InicioFirmaResponse>> startSignatureRequest(@PathVariable Long solicitudFirmaId) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.startSignatureRequest(solicitudFirmaId),
                "Inicio de firma invocado correctamente."
        ));
    }

    @GetMapping("/firmas/solicitudes/{solicitudFirmaId}")
    @PreAuthorize("@gdrAccessPolicyService.canAccessSignatureRequest(authentication, #solicitudFirmaId)")
    public ResponseEntity<ApiResponse<SolicitudFirmaDetalleResponse>> getSignatureRequest(@PathVariable Long solicitudFirmaId) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.getSignatureRequest(solicitudFirmaId),
                "Estado de la solicitud de firma consultado correctamente."
        ));
    }

    @GetMapping("/firmas/solicitudes/{solicitudFirmaId}/preparado")
    @PreAuthorize("@gdrAccessPolicyService.canAccessSignatureRequest(authentication, #solicitudFirmaId)")
    public ResponseEntity<?> previewPreparedDocument(
            @PathVariable Long solicitudFirmaId,
            @RequestParam(name = "descarga", defaultValue = "false") boolean download
    ) {
        return documentManagementService.previewPreparedDocument(solicitudFirmaId, download);
    }

    @PostMapping(path = "/firmas/solicitudes/{solicitudFirmaId}/retorno", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@gdrAccessPolicyService.canRegisterSignatureReturn(authentication, #solicitudFirmaId)")
    public ResponseEntity<ApiResponse<SolicitudFirmaDetalleResponse>> registerSignatureReturn(
            @PathVariable Long solicitudFirmaId,
            @Valid @ModelAttribute RegistrarRetornoFirmaRequest request,
            @RequestParam(name = "archivoFirmado", required = false) MultipartFile archivoFirmado,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.registerSignatureReturn(solicitudFirmaId, request, archivoFirmado, principal.getName()),
                "Retorno de firma registrado correctamente."
        ));
    }

    @PostMapping(path = "/firmados", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@gdrAccessPolicyService.canRegisterSignedDocumentsForEvaluated(authentication, #request.evaluatedId())")
    public ResponseEntity<ApiResponse<DocumentoFirmadoDetalleResponse>> registerSignedDocument(
            @Valid @ModelAttribute RegistrarDocumentoFirmadoRequest request,
            @RequestParam("archivo") MultipartFile archivo,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.registerSignedDocument(request, archivo, principal.getName()),
                "Documento firmado registrado correctamente."
        ));
    }

    @DeleteMapping("/firmados/{documentId}")
    @PreAuthorize("@gdrAccessPolicyService.canDeactivateSignedDocument(authentication, #documentId)")
    public ResponseEntity<ApiResponse<Void>> deactivateSignedDocument(@PathVariable Long documentId) {
        documentManagementService.deactivateSignedDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Documento firmado desactivado correctamente."));
    }

    @GetMapping("/firmados/{documentId}")
    @PreAuthorize("@gdrAccessPolicyService.canAccessDocumentById(authentication, #documentId)")
    public ResponseEntity<ApiResponse<DocumentoFirmadoDetalleResponse>> getSignedDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentManagementService.getSignedDocument(documentId),
                "Detalle del documento firmado consultado correctamente."
        ));
    }

    @GetMapping("/firmados/{documentId}/preview")
    @PreAuthorize("@gdrAccessPolicyService.canAccessDocumentById(authentication, #documentId)")
    public ResponseEntity<?> previewSignedDocument(
            @PathVariable Long documentId,
            @RequestParam(name = "descarga", defaultValue = "false") boolean download
    ) {
        return documentManagementService.previewSignedDocument(documentId, download);
    }
}
