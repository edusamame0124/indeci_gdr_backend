package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.InstitutionBranding;
import pe.gob.gdr.access.domain.repository.InstitutionBrandingRepository;

@Repository
public interface JpaInstitutionBrandingRepository
        extends JpaRepository<InstitutionBranding, Long>, InstitutionBrandingRepository {

    List<InstitutionBranding> findByStatusOrderByUpdatedAtDescIdDesc(String status);

    @Override
    default Optional<InstitutionBranding> findActiveBranding() {
        return findByStatusOrderByUpdatedAtDescIdDesc("ACTIVE").stream().findFirst();
    }
}
