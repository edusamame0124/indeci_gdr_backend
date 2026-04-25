package pe.gob.gdr.access.domain.repository;

import java.util.Optional;
import pe.gob.gdr.access.domain.model.InstitutionBranding;

public interface InstitutionBrandingRepository {

    Optional<InstitutionBranding> findActiveBranding();

    InstitutionBranding save(InstitutionBranding institutionBranding);
}
