package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.GdrInformeCierre;

public interface GdrInformeCierreRepository {

    Optional<GdrInformeCierre> findById(Long id);

    Optional<GdrInformeCierre> findLatestByCycleId(Long cycleId);

    List<GdrInformeCierre> findByCycleIdOrderByFechaGeneracionDesc(Long cycleId);

    GdrInformeCierre save(GdrInformeCierre informe);
}
