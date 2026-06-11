package pe.gob.gdr.access.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del endpoint de validación de acceso a un ciclo específico.
 * Usada por el frontend para determinar si el ciclo navegado es el activo
 * (isActiveCycle) y así decidir si mostrar o bloquear módulos backend-singleton.
 */
public record CycleAccessResponse(
        Long id,
        String code,
        String name,
        String status,
        String estadoEtapa,
        String estadoEtapaLabel,
        @JsonProperty("isActiveCycle") boolean isActiveCycle,
        boolean canAccess
) {
}
