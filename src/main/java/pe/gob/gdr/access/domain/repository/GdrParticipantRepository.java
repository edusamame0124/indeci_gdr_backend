package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;

import pe.gob.gdr.access.domain.model.GdrParticipant;

public interface GdrParticipantRepository {
    
    GdrParticipant save(GdrParticipant participant);
    
    Optional<GdrParticipant> findById(Long id);
    
    Optional<GdrParticipant> findByCycleIdAndPersonId(Long cycleId, Long personId);
    
    List<GdrParticipant> findByCycleId(Long cycleId);
    
    List<GdrParticipant> findByCycleIdAndRoleIn(Long cycleId, List<String> roles);
    
    void delete(GdrParticipant participant);
}
