package pe.gob.gdr.access.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.gob.gdr.access.domain.model.GdrParticipant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaGdrParticipantRepository extends JpaRepository<GdrParticipant, Long> {
    Optional<GdrParticipant> findByCycleIdAndPersonId(Long cycleId, Long personId);
    List<GdrParticipant> findByCycleId(Long cycleId);
    List<GdrParticipant> findByCycleIdAndRoleIn(Long cycleId, List<String> roles);
}
