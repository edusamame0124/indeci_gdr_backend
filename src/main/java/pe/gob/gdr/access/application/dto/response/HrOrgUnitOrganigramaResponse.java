package pe.gob.gdr.access.application.dto.response;

public record HrOrgUnitOrganigramaResponse(
        Long id,
        String code,
        String name,
        Long parentId,
        String parentName,
        Integer displayOrder
) {
}
