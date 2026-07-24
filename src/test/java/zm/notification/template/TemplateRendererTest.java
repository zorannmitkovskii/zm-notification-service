package zm.notification.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void emailVerification_carriesCodeAndBrand() {
        RenderedEmail r = renderer.render("EMAIL_VERIFICATION", "event-app", Map.of("code", "123456"));

        assertThat(r.subject()).contains("Ivy Events").containsIgnoringCase("verification");
        assertThat(r.htmlBody()).contains("123456");
    }

    @Test
    void passwordReset_carriesCode() {
        RenderedEmail r = renderer.render("PASSWORD_RESET", "event-app", Map.of("code", "654321"));

        assertThat(r.subject()).containsIgnoringCase("reset").contains("Ivy Events");
        assertThat(r.htmlBody()).contains("654321");
    }

    @Test
    void tempPassword_carriesPassword() {
        RenderedEmail r = renderer.render("TEMP_PASSWORD", "event-app", Map.of("tempPassword", "Abc123!"));

        assertThat(r.htmlBody()).contains("Abc123!");
    }

    @Test
    void raw_passesSubjectAndHtmlUnchanged() {
        RenderedEmail r = renderer.render("RAW", "event-app",
                Map.of("subject", "Hello", "html", "<p>Body</p>"));

        assertThat(r.subject()).isEqualTo("Hello");
        assertThat(r.htmlBody()).isEqualTo("<p>Body</p>");
    }

    @Test
    void unknownRealm_usesRealmNameAsBrand() {
        RenderedEmail r = renderer.render("EMAIL_VERIFICATION", "presmetko-app", Map.of("code", "1"));

        assertThat(r.subject()).contains("presmetko-app");
    }

    @Test
    void htmlInCode_isEscaped() {
        RenderedEmail r = renderer.render("EMAIL_VERIFICATION", "event-app", Map.of("code", "<b>x</b>"));

        assertThat(r.htmlBody()).contains("&lt;b&gt;x&lt;/b&gt;").doesNotContain("<b>x</b>");
    }

    @Test
    void missingRequiredParam_throws() {
        assertThatThrownBy(() -> renderer.render("EMAIL_VERIFICATION", "event-app", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code");
    }

    @Test
    void unknownTemplate_throws() {
        assertThatThrownBy(() -> renderer.render("NOPE", "event-app", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOPE");
    }
}
