package zm.notification.email;

/**
 * Pluggable outbound-email boundary. ZeptoMail is the first implementation;
 * SendGrid / SES / SMTP can be added without touching callers. Throws on any
 * non-success so the Kafka retry/dead-letter machinery can react.
 */
public interface EmailProvider {
    void send(String toEmail, String subject, String htmlBody);
}
