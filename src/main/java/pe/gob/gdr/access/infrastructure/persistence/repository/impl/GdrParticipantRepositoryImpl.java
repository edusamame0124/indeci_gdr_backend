package pe.gob.gdr.access.infrastructure.persistence.repository.impl;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

import pe.gob.gdr.access.domain.model.GdrParticipant;
import pe.gob.gdr.access.domain.repository.GdrParticipantRepository;
import pe.gob.gdr.access.infrastructure.persistence.repository.JpaGdrParticipantRepository;

@Component
public class GdrParticipantRepositoryImpl implements GdrParticipantRepository {

    private final JpaGdrParticipantRepository jpaRepository;

    public GdrParticipantRepositoryImpl(JpaGdrParticipantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GdrParticipant save(GdrParticipant participant) {
        return jpaRepository.save(participant);
    }

    @Override
    public Optional<GdrParticipant> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<GdrParticipant> findByCycleIdAndPersonId(Long cycleId, Long personId) {
        return jpaRepository.findByCycleIdAndPersonId(cycleId, personId);
    }

    @Override
    public List<GdrParticipant> findByCycleId(Long cycleId) {
        return jpaRepository.findByCycleId(cycleId);
    }

    @Override
    public List<GdrParticipant> findByCycleIdAndRoleIn(Long cycleId, List<String> roles) {
        return jpaRepository.findByCycleIdAndRoleIn(cycleId, roles);
    }

    @Override
    public void delete(GdrParticipant participant) {
        jpaRepository.delete(participant);
    }
}
