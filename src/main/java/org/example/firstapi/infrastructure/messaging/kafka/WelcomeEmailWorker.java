package org.example.firstapi.infrastructure.messaging.kafka;

import org.example.firstapi.application.events.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEmailWorker {
    private final JavaMailSender mailSender;

    public WelcomeEmailWorker(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(
            topics = "#{T(org.example.firstapi.application.events.EventTopic).USER_REGISTERED.topicName()}",
            groupId = "welcome-email-worker"
    )
    public void handle(UserRegisteredEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@springtodo.local");
        message.setTo(event.email());
        message.setSubject("Witamy w SpringToDo App!");
        message.setText("Witaj %s %s w SpringToDo App!".formatted(event.firstName(), event.lastName()));
        mailSender.send(message);
    }
}
