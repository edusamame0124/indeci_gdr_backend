package pe.gob.gdr.access.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.AuditEventResponse;
import pe.gob.gdr.access.application.dto.response.PageResponse;
import pe.gob.gdr.access.application.service.AuditTrailQueryService;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditTrailQueryService auditTrailQueryService;

    public AuditController(AuditTrailQueryService auditTrailQueryService) {
        this.auditTrailQueryService = auditTrailQueryService;
    }

    @GetMapping("/events")
    @PreAuthorize("@gdrAccessPolicyService.canViewAuditoria(authentication)")
    public ResponseEntity<ApiResponse<PageResponse<AuditEventResponse>>> listEvents(
            @RequestParam(name = "eventCode", required = false) String eventCode,
            @RequestParam(name = "principal", required = false) String principal,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(ApiResponse.ok(
                auditTrailQueryService.buscar(eventCode, principal, from, to, PageRequest.of(safePage, safeSize)),
                "Eventos de auditoría consultados."
        ));
    }

    @GetMapping("/events/exportar")
    @PreAuthorize("@gdrAccessPolicyService.canViewAuditoria(authentication)")
    public ResponseEntity<ByteArrayResource> exportEvents(
            @RequestParam(name = "eventCode", required = false) String eventCode,
            @RequestParam(name = "principal", required = false) String principal,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            Principal principalUser
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String username = principalUser != null ? principalUser.getName() : "sistema-gdr";
        byte[] content = auditTrailQueryService.exportCsv(
                eventCode, principal, from, to, PageRequest.of(safePage, safeSize), username
        );
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("auditoria_gdr.csv", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(content));
    }
}
