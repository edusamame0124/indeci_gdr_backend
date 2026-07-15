package pe.gob.gdr.access.application.dto.response;

/**
 * Opcion de persona para el buscador de "Asignar Rol GDR".
 *
 * Puede provenir de dos origenes:
 *  - LOCAL: persona ya aprovisionada en GDR ({@code personId} no nulo).
 *  - SISRH: usuario GDR_USUARIO del SISRH aun no aprovisionado en GDR
 *    ({@code personId} nulo; se aprovisiona al asignar el rol usando
 *    {@code documentNumber}, {@code displayName}, {@code username} y
 *    {@code orgUnitCode} = codigo de oficina del SISRH).
 */
public record AssignmentPersonOptionResponse(
        Long personId,
        String documentNumber,
        String displayName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String username,
        String origin
) {
    public static final String ORIGIN_LOCAL = "LOCAL";
    public static final String ORIGIN_SISRH = "SISRH";
}
