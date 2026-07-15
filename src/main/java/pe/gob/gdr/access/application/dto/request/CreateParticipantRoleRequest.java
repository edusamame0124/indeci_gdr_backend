package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para asignar un rol GDR a una persona dentro de un ciclo.
 *
 * La persona puede identificarse de dos formas:
 *  - {@code personId}: persona ya aprovisionada en GDR (candidato LOCAL).
 *  - Datos del SISRH ({@code documentNumber}, {@code displayName},
 *    {@code username}, {@code orgUnitCode}): candidato del directorio SISRH aun
 *    no aprovisionado en GDR. En ese caso {@code personId} viaja nulo y GDR
 *    aprovisiona la ficha local al asignar el rol.
 */
public record CreateParticipantRoleRequest(
    @NotNull(message = "El ID del ciclo es obligatorio.") Long cycleId,
    Long personId,
    @NotNull(message = "El rol es obligatorio.") String role,
    String documentNumber,
    String displayName,
    String username,
    String orgUnitCode
) {}
