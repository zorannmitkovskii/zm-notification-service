package zm.notification.consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zm.notification.NotificationService;
import zm.notification.event.Channel;
import zm.notification.event.NotificationRequested;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Wiring check against a real broker: the listener starts and reads the plain-map payload IAM sends.
 */
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = "zm.notifications.v1")
class NotificationConsumerKafkaIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private NotificationService service;

    @Test
    void consumesTheEventShapeIamPublishes() {
        Map<String, Object> event = new HashMap<>();
        event.put("channel", "EMAIL");
        event.put("template", "EMAIL_VERIFICATION");
        event.put("to", "u@x.mk");
        event.put("realm", "event-app");
        event.put("params", Map.of("code", "123456"));
        event.put("idempotencyKey", "verify:event-app:u@x.mk:123456");

        kafkaTemplate.send("zm.notifications.v1", "u@x.mk", event);

        ArgumentCaptor<NotificationRequested> received = ArgumentCaptor.forClass(NotificationRequested.class);
        verify(service, timeout(30_000)).handle(received.capture());
        assertThat(received.getValue().channel()).isEqualTo(Channel.EMAIL);
        assertThat(received.getValue().to()).isEqualTo("u@x.mk");
        assertThat(received.getValue().params()).containsEntry("code", "123456");
    }
}
