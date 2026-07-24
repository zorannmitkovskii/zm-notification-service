package zm.notification.template;

/** Output of {@link TemplateRenderer}: a subject + HTML body ready to hand
 *  to an {@link zm.notification.email.EmailProvider}. */
public record RenderedEmail(String subject, String htmlBody) {}
