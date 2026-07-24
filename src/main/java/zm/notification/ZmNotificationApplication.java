package zm.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ZmNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZmNotificationApplication.class, args);
    }
}
