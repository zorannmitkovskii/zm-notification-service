package zm.notification.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * The single event every ZM service emits to request a notification. Kept
 * channel-agnostic so SMS/push can be added without changing producers:
 * the {@code channel} + {@code template} decide how it's rendered and sent.
 *
 * <p>Producers NEVER render or send — they only describe intent:
 * <pre>
 * { "channel": "EMAIL", "template": "EMAIL_VERIFICATION",
 *   "to": "user@x.mk", "tenantId": "...", "realm": "event-app",
 *   "params": { "code": "123456", "name": "Zoran" },
 *   "idempotencyKey": "verify:event-app:user@x.mk:123456" }
 * </pre>
 *
 * @param channel        delivery channel (EMAIL today)
 * @param template       template id the service knows how to render
 * @param to             recipient address (email / phone) for the channel
 * @param realm          Keycloak realm / product the user belongs to (branding)
 * @param tenantId       tenant the notification is scoped to (multi-tenant)
 * @param params         template variables (code, name, links, …)
 * @param idempotencyKey de-dupes redelivered events; null → best effort
 */
public record NotificationRequested(
        @NotNull Channel channel,
        @NotBlank String template,
        @NotBlank String to,
        String realm,
        String tenantId,
        Map<String, Object> params,
        String idempotencyKey
) {
    public Map<String, Object> paramsOrEmpty() {
        return params == null ? Map.of() : params;
    }
}
