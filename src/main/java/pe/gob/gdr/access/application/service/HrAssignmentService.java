package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.HrAssignmentSummaryResponse;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;

@Service
public class HrAssignmentService {

    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public HrAssignmentService(
            GdrEvaluationAssignmentRepository assignmentRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    public List<HrAssignmentSummaryResponse> listAssignments(String username) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        List<GdrEvaluationAssignment> assignments = gdrAccessPolicyService.isAdminSistema(user) || gdrAccessPolicyService.isOrh(user)
                ? assignmentRepository.findActiveAssignmentsForActiveCycle()
                : assignmentRepository.findActiveByPersonIdInActiveCycle(user.getPerson().getId());

        return assignments.stream()
                .map(this::mapSummary)
                .toList();
    }

    private HrAssignmentSummaryResponse mapSummary(GdrEvaluationAssignment assignment) {
        GdrSegment segment = assignment.getSegment();
        return new HrAssignmentSummaryResponse(
                assignment.getId(),
                assignment.getCycle().getId(),
                assignment.getCycle().getCode(),
                assignment.getCycle().getName(),
                assignment.getEvaluatorPerson().getId(),
                assignment.getEvaluatorPerson().getDisplayName(),
                assignment.getEvaluatedPerson().getId(),
                assignment.getEvaluatedPerson().getDisplayName(),
                assignment.getEvaluatedPerson().getOrgUnit().getId(),
                assignment.getEvaluatedPerson().getOrgUnit().getCode(),
                assignment.getEvaluatedPerson().getOrgUnit().getName(),
                segment != null ? segment.getId() : null,
                segment != null ? segment.getCode() : null,
                segment != null ? segment.getName() : null,
                assignment.getStatus()
        );
    }
}
