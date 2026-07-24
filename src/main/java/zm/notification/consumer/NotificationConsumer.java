package zm.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import zm.notification.NotificationService;
import zm.notification.event.NotificationRequested;

/**
 * Consumes {@code zm.notifications.v1}. Non-blocking for producers: they fire
 * the event and move on — this service does the slow provider call.
 *
 * <p>{@link RetryableTopic} gives us Spring Kafka's retry + dead-letter for
 * free: transient failures (provider down) retry with backoff on
 * {@code *-retry} topics; exhausted messages land on {@code *-dlt} for
 * inspection instead of blocking the partition.
 */
@Slf4j
@Component
public class NotificationConsumer {

    private final NotificationService service;

    public NotificationConsumer(NotificationService service) {
        this.service = service;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 3.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${notification.topic:zm.notifications.v1}",
            groupId = "${spring.kafka.consumer.group-id:zm-notification-service}")
    public void onNotification(NotificationRequested event) {
        log.debug("[Notification] Received channel={} template={} to={}",
                event.channel(), event.template(), event.to());
        service.handle(event);
    }
}
