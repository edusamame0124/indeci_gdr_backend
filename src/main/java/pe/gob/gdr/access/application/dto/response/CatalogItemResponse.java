package pe.gob.gdr.access.application.dto.response;

public record CatalogItemResponse(
        Long id,
        String code,
        String name,
        String description
) {
}
