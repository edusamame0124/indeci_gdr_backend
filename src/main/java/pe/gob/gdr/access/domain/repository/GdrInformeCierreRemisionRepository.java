package pe.gob.gdr.access.domain.repository;

import java.util.List;
import pe.gob.gdr.access.domain.model.GdrInformeCierreRemision;

public interface GdrInformeCierreRemisionRepository {

    GdrInformeCierreRemision save(GdrInformeCierreRemision remision);

    List<GdrInformeCierreRemision> findByInformeCierreIdOrderByFechaRemisionDesc(Long informeId);
}
