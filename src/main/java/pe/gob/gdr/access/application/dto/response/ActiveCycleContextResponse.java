package pe.gob.gdr.access.application.dto.response;

public record ActiveCycleContextResponse(
        Long cycleId,
        String cycleCode,
        String cycleName,
        String contextCode,
        String contextName,
        boolean cycleActive,
        boolean assigned,
        boolean hrPersonLinked,
        Long personId,
        String personDocumentNumber,
        String personDisplayName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String functionalActor,
        String operationalScope,
        boolean gdrOperational
) {

    public static ActiveCycleContextResponse empty() {
        return new ActiveCycleContextResponse(
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                "SIN_ROL_FUNCIONAL_GDR",
                "SIN_ACCESO_GDR",
                false
        );
    }
}
