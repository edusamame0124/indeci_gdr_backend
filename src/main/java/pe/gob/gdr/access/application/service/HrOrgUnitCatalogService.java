package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.HrOrgUnitOrganigramaResponse;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.repository.HrOrgUnitRepository;

@Service
public class HrOrgUnitCatalogService {

    private final HrOrgUnitRepository hrOrgUnitRepository;

    public HrOrgUnitCatalogService(HrOrgUnitRepository hrOrgUnitRepository) {
        this.hrOrgUnitRepository = hrOrgUnitRepository;
    }

    public Page<HrOrgUnitOrganigramaResponse> pageOrganigramaForSigning(Pageable pageable) {
        return hrOrgUnitRepository.findPageActiveOrganigrama(pageable).map(this::toOrganigrama);
    }

    /**
     * Oficinas para el selector de firma: códigos con prefijo {@code OF_} (organigrama V73) o código institucional {@code OTI} (seed HR base).
     */
    public List<HrOrgUnitOrganigramaResponse> listOfficesForSigning() {
        return hrOrgUnitRepository.findAllActiveOfficesForOrganigrama().stream()
                .map(this::toOrganigrama)
                .toList();
    }

    private HrOrgUnitOrganigramaResponse toOrganigrama(HrOrgUnit unit) {
        HrOrgUnit parent = unit.getParent();
        return new HrOrgUnitOrganigramaResponse(
                unit.getId(),
                unit.getCode(),
                unit.getName(),
                parent != null ? parent.getId() : null,
                parent != null ? parent.getName() : null,
                unit.getDisplayOrder()
        );
    }
}
