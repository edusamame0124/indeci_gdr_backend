package pe.gob.gdr.access.application.service;

import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class UserContextService {

    private final UserRepository userRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public UserContextService(
            UserRepository userRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.userRepository = userRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    public ActiveCycleContextResponse resolveCurrentContext(String username) {
        return userRepository.findByUsernameWithPerson(username)
                .map(gdrAccessPolicyService::resolveContext)
                .orElseGet(ActiveCycleContextResponse::empty);
    }
}
