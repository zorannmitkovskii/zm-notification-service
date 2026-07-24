package zm.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.notification.email.EmailProvider;
import zm.notification.event.Channel;
import zm.notification.event.NotificationRequested;
import zm.notification.template.RenderedEmail;
import zm.notification.template.TemplateRenderer;

/**
 * Turns a {@link NotificationRequested} event into an actual send: pick the
 * channel, render the template, dispatch through the provider. Thin on
 * purpose — rendering lives in {@link TemplateRenderer}, delivery in the
 * {@link EmailProvider}. Throws on failure so the consumer retries / DLTs.
 */
@Slf4j
@Service
public class NotificationService {

    private final TemplateRenderer renderer;
    private final EmailProvider emailProvider;

    public NotificationService(TemplateRenderer renderer, EmailProvider emailProvider) {
        this.renderer = renderer;
        this.emailProvider = emailProvider;
    }

    public void handle(NotificationRequested event) {
        if (event.channel() != Channel.EMAIL) {
            // SMS/PUSH not wired yet — skip rather than fail the whole partition.
            log.warn("[Notification] Channel {} not implemented yet — dropping (template={}, to={})",
                    event.channel(), event.template(), event.to());
            return;
        }
        RenderedEmail email = renderer.render(event.template(), event.realm(), event.paramsOrEmpty());
        emailProvider.send(event.to(), email.subject(), email.htmlBody());
        log.info("[Notification] EMAIL sent template={} realm={} to={}",
                event.template(), event.realm(), event.to());
    }
}
