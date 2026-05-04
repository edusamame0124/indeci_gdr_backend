package pe.gob.gdr.access.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pe.gob.gdr.access.application.dto.response.NotificarCalificacionMailResponse;
import pe.gob.gdr.access.application.mail.GdrFinalQualificationNoticeComposer;
import pe.gob.gdr.access.application.mail.GdrFinalQualificationNoticeComposer.ComposedMail;
import pe.gob.gdr.access.application.port.OutboundMailMessage;
import pe.gob.gdr.access.application.port.OutboundMailPort;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.EvalQualEmailLogStatus;
import pe.gob.gdr.access.domain.model.GdrEvalQualEmailLog;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrEvalQualEmailLogRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class GdrFinalEvaluationQualificationNotifyService {

    private static final int ERROR_MESSAGE_MAX_LEN = 1000;
    private static final String ESTADO_ENVIADO = "ENVIADO";

    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrEvalQualEmailLogRepository qualEmailLogRepository;
    private final UserRepository userRepository;
    private final OutboundMailPort outboundMailPort;
    private final GdrQualificationNotifySchedulePolicy qualificationNotifySchedulePolicy;

    public GdrFinalEvaluationQualificationNotifyService(
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrEvalQualEmailLogRepository qualEmailLogRepository,
            UserRepository userRepository,
            OutboundMailPort outboundMailPort,
            GdrQualificationNotifySchedulePolicy qualificationNotifySchedulePolicy
    ) {
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.qualEmailLogRepository = qualEmailLogRepository;
        this.userRepository = userRepository;
        this.outboundMailPort = outboundMailPort;
        this.qualificationNotifySchedulePolicy = qualificationNotifySchedulePolicy;
    }

    @Transactional(noRollbackFor = DomainException.class)
    public NotificarCalificacionMailResponse notifyByEvaluator(String actorUsername, Long evaluationId) {
        GdrFinalEvaluation evaluation = finalEvaluationRepository.findByIdInActiveCycle(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la evaluación final activa indicada."));
        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario autenticado."));

        String ratingCode = evaluation.getQualitativeRatingCode();
        if (!StringUtils.hasText(ratingCode)) {
            throw new DomainException("La evaluación final debe tener calificación cualitativa registrada antes de notificar.");
        }

        qualificationNotifySchedulePolicy.assertMailNotifyAllowedToday(evaluation.getAssignment().getCycle());

        HrPerson evaluatedPerson = evaluation.getAssignment().getEvaluatedPerson();
        String recipientEmail = resolveRecipientEmail(evaluatedPerson.getId(), evaluatedPerson);
        if (!isPlausibleEmail(recipientEmail)) {
            throw new DomainException(
                    "No hay un correo electrónico válido para la persona evaluada; actualice RRHH o el vínculo de usuario institucional."
            );
        }

        ComposedMail composed =
                GdrFinalQualificationNoticeComposer.compose(
                        evaluation.getAssignment().getCycle().getName(),
                        evaluatedPerson.getDisplayName(),
                        evaluation.getQualitativeRatingCode(),
                        evaluation.getConsolidatedScore());

        OutboundMailMessage mailMessage =
                new OutboundMailMessage(null, recipientEmail, composed.subject(), composed.textBody(), composed.htmlBody());

        try {
            outboundMailPort.send(mailMessage);
            qualEmailLogRepository.save(
                    buildLog(evaluation, actor, recipientEmail, composed.subject(), EvalQualEmailLogStatus.ENVIADO, null));
            return new NotificarCalificacionMailResponse(ESTADO_ENVIADO, GdrFinalQualificationNoticeComposer.TEMPLATE_CODE);
        } catch (DomainException sendFailure) {
            qualEmailLogRepository.save(
                    buildLog(
                            evaluation,
                            actor,
                            recipientEmail,
                            composed.subject(),
                            EvalQualEmailLogStatus.FALLIDO,
                            truncateError(sendFailure.getMessage())));
            throw sendFailure;
        }
    }

    private GdrEvalQualEmailLog buildLog(
            GdrFinalEvaluation evaluation,
            User actor,
            String recipientEmail,
            String subject,
            EvalQualEmailLogStatus status,
            String errorMessage
    ) {
        return GdrEvalQualEmailLog.builder()
                .finalEvaluation(evaluation)
                .recipientEmail(recipientEmail)
                .templateCode(GdrFinalQualificationNoticeComposer.TEMPLATE_CODE)
                .subject(subject)
                .status(status)
                .errorMessage(errorMessage)
                .actionUser(actor)
                .build();
    }

    private String resolveRecipientEmail(Long evaluatedPersonId, HrPerson evaluatedPerson) {
        String fromHr = normalizeEmail(evaluatedPerson.getEmail());
        if (isPlausibleEmail(fromHr)) {
            return fromHr;
        }
        return userRepository.findActiveGdrUsersByPersonId(evaluatedPersonId).stream()
                .map(User::getEmail)
                .map(this::normalizeEmail)
                .filter(this::isPlausibleEmail)
                .findFirst()
                .orElse("");
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isPlausibleEmail(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String trimmed = value.trim();
        int at = trimmed.indexOf('@');
        return at > 0 && at < trimmed.length() - 1 && trimmed.indexOf('@', at + 1) < 0;
    }

    private static String truncateError(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= ERROR_MESSAGE_MAX_LEN) {
            return message;
        }
        return message.substring(0, ERROR_MESSAGE_MAX_LEN - 3) + "...";
    }
}
