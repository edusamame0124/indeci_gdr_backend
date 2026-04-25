package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CatalogItemResponse;
import pe.gob.gdr.access.application.service.GdrCatalogService;

@RestController
@RequestMapping("/admin/catalogs")
public class GdrCatalogController {

    private final GdrCatalogService gdrCatalogService;

    public GdrCatalogController(GdrCatalogService gdrCatalogService) {
        this.gdrCatalogService = gdrCatalogService;
    }

    @GetMapping("/value-types")
    @PreAuthorize("@gdrAccessPolicyService.canViewCatalogs(authentication)")
    public ResponseEntity<ApiResponse<List<CatalogItemResponse>>> listValueTypes() {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrCatalogService.listValueTypes(),
                "Tipos de valor consultados correctamente."
        ));
    }

    @GetMapping("/formulas")
    @PreAuthorize("@gdrAccessPolicyService.canViewCatalogs(authentication)")
    public ResponseEntity<ApiResponse<List<CatalogItemResponse>>> listFormulas() {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrCatalogService.listFormulas(),
                "Formulas consultadas correctamente."
        ));
    }

    @GetMapping("/segments")
    @PreAuthorize("@gdrAccessPolicyService.canViewCatalogs(authentication)")
    public ResponseEntity<ApiResponse<List<CatalogItemResponse>>> listSegments() {
        return ResponseEntity.ok(ApiResponse.ok(
                gdrCatalogService.listSegments(),
                "Segmentos consultados correctamente."
        ));
    }
}
