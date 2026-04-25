package pe.gob.gdr.access.application.dto.response;

public record IndicatorResponse(
        Long id,
        String code,
        String name,
        String description,
        Long valueTypeId,
        String valueTypeName,
        Long formulaId,
        String formulaName,
        Long segmentId,
        String segmentName,
        String status
) {
}
