package pe.gob.gdr.access.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pe.gob.gdr.access.application.port.OutboundMailMessage;
import pe.gob.gdr.access.application.port.OutboundMailPort;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.infrastructure.config.AppMailProperties;

@Component
public class SpringJavaMailSenderAdapter implements OutboundMailPort {

    private static final Logger log = LoggerFactory.getLogger(SpringJavaMailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final AppMailProperties mailProperties;

    public SpringJavaMailSenderAdapter(JavaMailSender mailSender, AppMailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(OutboundMailMessage message) {
        Objects.requireNonNull(message, "message");
        String resolvedFrom = resolveFrom(message);
        if (!StringUtils.hasText(message.to())) {
            throw new DomainException("El destinatario del correo es obligatorio.");
        }
        if (!StringUtils.hasText(message.subject())) {
            throw new DomainException("El asunto del correo es obligatorio.");
        }
        boolean hasText = StringUtils.hasText(message.textBody());
        boolean hasHtml = StringUtils.hasText(message.htmlBody());
        if (!hasText && !hasHtml) {
            throw new DomainException("El cuerpo del correo (texto o HTML) es obligatorio.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            boolean multipart = hasText && hasHtml;
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, multipart, StandardCharsets.UTF_8.name());
            helper.setFrom(Objects.requireNonNull(resolvedFrom, "from"));
            String to = Objects.requireNonNull(message.to(), "to");
            helper.setTo(to);
            String subject = Objects.requireNonNull(message.subject(), "subject");
            helper.setSubject(subject);
            if (hasText && hasHtml) {
                helper.setText(
                        Objects.requireNonNull(message.textBody(), "textBody"),
                        Objects.requireNonNull(message.htmlBody(), "htmlBody"));
            } else if (hasHtml) {
                helper.setText(Objects.requireNonNull(message.htmlBody(), "htmlBody"), true);
            } else {
                helper.setText(Objects.requireNonNull(message.textBody(), "textBody"), false);
            }
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException ex) {
            log.warn("Fallo al preparar o enviar correo SMTP: {}", ex.getMessage());
            throw new DomainException("No fue posible enviar el correo.");
        }
    }

    private String resolveFrom(OutboundMailMessage message) {
        if (StringUtils.hasText(message.from())) {
            return message.from();
        }
        if (StringUtils.hasText(mailProperties.getDefaultFrom())) {
            return mailProperties.getDefaultFrom();
        }
        throw new DomainException("No hay remitente configurado para el envío de correo.");
    }
}
