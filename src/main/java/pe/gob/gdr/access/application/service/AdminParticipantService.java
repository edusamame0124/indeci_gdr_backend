package pe.gob.gdr.access.application.service;

import java.util.List;
import pe.gob.gdr.access.application.dto.request.CreateParticipantRoleRequest;
import pe.gob.gdr.access.application.dto.response.ParticipantResponse;

public interface AdminParticipantService {
    ParticipantResponse assignRole(CreateParticipantRoleRequest request);
    ParticipantResponse updateRole(Long participantId, String role);
    List<ParticipantResponse> listParticipantsByCycle(Long cycleId);
    List<pe.gob.gdr.access.application.dto.response.ParticipantSummaryResponse> summaryByCycle(Long cycleId);
    ParticipantResponse updateStatus(Long participantId, String status);
}
