package pe.gob.gdr.access.application.port;

/** Technical payload for SMTP send; application layer builds content and addresses per business rules. */
public record OutboundMailMessage(String from, String to, String subject, String textBody, String htmlBody) {}
