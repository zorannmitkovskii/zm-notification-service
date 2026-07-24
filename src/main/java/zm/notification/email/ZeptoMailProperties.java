package zm.notification.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ZeptoMail provider config, bound from {@code notification.email.zepto.*}.
 * The API token lives ONLY here now — this service is the single owner of the
 * email-provider secret (it used to be duplicated in ivy-be + IAM).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "notification.email.zepto")
public class ZeptoMailProperties {

    /** Sent verbatim as the {@code Authorization} header (already includes the
     *  {@code Zoho-enczapikey } prefix). */
    private String token;
    private String fromAddress;
    private String fromName;
    private String baseUrl = "https://api.zeptomail.eu";
}
