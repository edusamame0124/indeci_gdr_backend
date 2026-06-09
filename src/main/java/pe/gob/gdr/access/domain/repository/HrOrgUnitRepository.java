package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.gdr.access.domain.model.HrOrgUnit;

public interface HrOrgUnitRepository {

    List<HrOrgUnit> findAllActiveOrdered();

    Optional<HrOrgUnit> findActiveById(Long id);

    /**
     * SSO — resuelve la oficina activa por su {@code UNIT_CODE}. Usado al
     * aprovisionar un {@code HrPerson} desde el claim {@code areas.rendimiento}
     * del token SISRH (UNIT_CODE == CODIGO_AREA en INDECI_SISTEMA_AREA).
     */
    Optional<HrOrgUnit> findActiveByCode(String code);

    Page<HrOrgUnit> findPageActiveOrganigrama(Pageable pageable);

    /**
     * Oficinas activas para selector de firma: prefijo {@code OF_} en {@code UNIT_CODE} o código {@code OTI} (oficina TI del seed base).
     */
    List<HrOrgUnit> findAllActiveOfficesForOrganigrama();
}

