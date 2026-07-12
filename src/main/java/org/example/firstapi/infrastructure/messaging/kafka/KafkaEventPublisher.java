package org.example.firstapi.infrastructure.messaging.kafka;

import org.example.firstapi.application.events.EventPublisher;
import org.example.firstapi.application.events.ApplicationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public <T extends ApplicationEvent> void publish(T event) {
        kafkaTemplate.send(event.topic().topicName(), event).join();
    }
}
