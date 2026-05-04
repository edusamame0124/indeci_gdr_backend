package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AsignarDistinguidoRequest(
        @NotEmpty List<Long> finalEvaluationIds
) {
}
