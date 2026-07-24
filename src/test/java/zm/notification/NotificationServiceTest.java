package zm.notification;

import org.junit.jupiter.api.Test;
import zm.notification.email.EmailProvider;
import zm.notification.event.Channel;
import zm.notification.event.NotificationRequested;
import zm.notification.template.RenderedEmail;
import zm.notification.template.TemplateRenderer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private final TemplateRenderer renderer = mock(TemplateRenderer.class);
    private final EmailProvider provider = mock(EmailProvider.class);
    private final NotificationService service = new NotificationService(renderer, provider);

    @Test
    void emailChannel_rendersThenSends() {
        when(renderer.render("EMAIL_VERIFICATION", "event-app", Map.of("code", "1")))
                .thenReturn(new RenderedEmail("Subj", "<p>1</p>"));
        NotificationRequested event = new NotificationRequested(
                Channel.EMAIL, "EMAIL_VERIFICATION", "u@x.mk", "event-app", "t1",
                Map.of("code", "1"), "idem-1");

        service.handle(event);

        verify(provider).send("u@x.mk", "Subj", "<p>1</p>");
    }

    @Test
    void nonEmailChannel_isDroppedWithoutRenderingOrSending() {
        NotificationRequested event = new NotificationRequested(
                Channel.SMS, "X", "u@x.mk", "event-app", null, Map.of(), null);

        service.handle(event);

        verifyNoInteractions(renderer, provider);
    }

    @Test
    void providerFailure_propagates() {
        when(renderer.render(any(), any(), any())).thenReturn(new RenderedEmail("S", "B"));
        doThrow(new RuntimeException("boom")).when(provider).send(any(), any(), any());
        NotificationRequested event = new NotificationRequested(
                Channel.EMAIL, "RAW", "u@x.mk", "event-app", null, Map.of(), null);

        assertThatThrownBy(() -> service.handle(event)).hasMessage("boom");
    }

    @Test
    void nullParams_areHandledAsEmpty() {
        when(renderer.render("RAW", "event-app", Map.of())).thenReturn(new RenderedEmail("S", "B"));
        NotificationRequested event = new NotificationRequested(
                Channel.EMAIL, "RAW", "u@x.mk", "event-app", null, null, null);

        service.handle(event);

        verify(provider).send("u@x.mk", "S", "B");
    }
}
