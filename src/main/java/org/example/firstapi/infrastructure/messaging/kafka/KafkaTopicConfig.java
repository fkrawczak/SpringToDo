package org.example.firstapi.infrastructure.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.firstapi.application.events.EventTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Arrays;

@Configuration
public class KafkaTopicConfig {
    @Bean
    KafkaAdmin.NewTopics applicationEventTopics() {
        NewTopic[] topics = Arrays.stream(EventTopic.values())
                .map(topic -> TopicBuilder.name(topic.topicName()).partitions(1).replicas(1).build())
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }
}
