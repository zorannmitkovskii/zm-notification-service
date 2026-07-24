package zm.notification.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ZeptoMail-backed {@link EmailProvider} — {@code POST {base}/v1.1/email}.
 * Throws {@link EmailSendException} on any network / non-2xx failure so the
 * consumer's retry + dead-letter topic can handle it.
 */
@Slf4j
@Component
public class ZeptoMailProvider implements EmailProvider {

    private final ZeptoMailProperties props;
    private final ObjectMapper mapper;
    private final HttpClient http;

    public ZeptoMailProvider(ZeptoMailProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public void send(String toEmail, String subject, String htmlBody) {
        if (!StringUtils.hasText(props.getToken())) {
            throw new EmailSendException("ZeptoMail token not configured (notification.email.zepto.token)");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(props.getBaseUrl()) + "/v1.1/email"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", props.getToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload(toEmail, subject, htmlBody)))
                .build();

        HttpResponse<String> resp;
        try {
            resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new EmailSendException("ZeptoMail network failure: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailSendException("Interrupted sending email", e);
        }

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new EmailSendException("ZeptoMail returned " + resp.statusCode() + ": " + snippet(resp.body()));
        }
        log.info("[ZeptoMail] Sent '{}' to {}", subject, toEmail);
    }

    private String payload(String toEmail, String subject, String htmlBody) {
        Map<String, Object> body = Map.of(
                "from", Map.of("address", props.getFromAddress(), "name", props.getFromName()),
                "to", List.of(Map.of("email_address", Map.of("address", toEmail))),
                "subject", subject,
                "htmlbody", htmlBody);
        try {
            return mapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new EmailSendException("Could not serialise ZeptoMail payload", e);
        }
    }

    private static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String snippet(String s) {
        if (s == null) return "<null>";
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }
}
