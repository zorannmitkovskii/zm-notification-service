package zm.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Owns the transactional email templates — the whole point of centralising
 * notifications here. Producers send a {@code template} id + {@code params};
 * this renders the subject + HTML body. Brand strings come from the realm so
 * one template serves every product.
 *
 * <p>Deliberately a plain switch for now (few templates, no external engine).
 * When the set grows, swap the body for a real engine (Thymeleaf / Mustache)
 * behind this same interface — callers don't change.
 */
@Component
public class TemplateRenderer {

    public RenderedEmail render(String template, String realm, Map<String, Object> params) {
        String brand = brandFor(realm);
        return switch (template) {
            // Pre-rendered by the caller (e.g. ivy-events-be's rich domain
            // emails). We just dispatch subject + html as-is. A stepping stone
            // until those templates move here too.
            case "RAW" -> new RenderedEmail(str(params, "subject"), str(params, "html"));
            case "EMAIL_VERIFICATION" -> new RenderedEmail(
                    "Your " + brand + " verification code",
                    wrap("<p>Your verification code is:</p>"
                            + code(str(params, "code"))
                            + "<p>It expires in 10 minutes.</p>"));
            case "PASSWORD_RESET" -> new RenderedEmail(
                    "Reset your " + brand + " password",
                    wrap("<p>To reset your password, enter this code:</p>"
                            + code(str(params, "code"))
                            + "<p>It expires in 10 minutes. If you didn't request a reset, ignore this email.</p>"));
            case "TEMP_PASSWORD" -> new RenderedEmail(
                    "Your " + brand + " account",
                    wrap("<p>Your account has been created. Use this temporary password to log in:</p>"
                            + code(str(params, "tempPassword"))
                            + "<p>You'll be asked to change it after your first login.</p>"));
            default -> throw new IllegalArgumentException("Unknown email template: " + template);
        };
    }

    private static String brandFor(String realm) {
        if (realm == null) return "ZM";
        return switch (realm) {
            case "event-app" -> "Ivy Events";
            default -> realm;
        };
    }

    private static String str(Map<String, Object> params, String key) {
        Object v = params == null ? null : params.get(key);
        if (v == null) throw new IllegalArgumentException("Missing template param '" + key + "'");
        return String.valueOf(v);
    }

    private static String code(String value) {
        return "<p style=\"font-size:20px;font-weight:bold;letter-spacing:3px;\">"
                + escape(value) + "</p>";
    }

    private static String wrap(String inner) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:15px;color:#222;\">" + inner + "</div>";
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
