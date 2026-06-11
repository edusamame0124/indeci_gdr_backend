package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrCronograma;

public interface GdrCronogramaRepository {

    List<GdrCronograma> findByCycleIdOrderByFechaInicio(Long cycleId);

    Optional<GdrCronograma> findByCycleIdAndEtapa(Long cycleId, String etapa);

    Optional<GdrCronograma> findById(Long id);

    GdrCronograma save(GdrCronograma cronograma);

    void deleteById(Long id);
}
