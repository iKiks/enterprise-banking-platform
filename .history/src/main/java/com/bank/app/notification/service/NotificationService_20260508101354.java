package com.bank.app.notification.service;

import com.bank.app.notification.entity.Notification;
import com.bank.app.notification.repository.NotificationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationService(NotificationRepository notificationRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishNotification(@NonNull String recipient, @NonNull String subject,
                                    @NonNull String body, @NonNull String channel) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setChannel(channel);
        notification.setDelivered(false);
        notificationRepository.save(notification);

        kafkaTemplate.send("banking.notification", recipient, notification);
    }
}
