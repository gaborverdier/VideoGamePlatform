package com.gaming.platform.service;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gaming.api.models.NotificationModel;
import com.gaming.platform.model.Notification;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsService {

    private final NotificationRepository notificationRepository;
    private final EventProducer eventProducer;

    public List<NotificationModel> getUserNotifications(String userId) {
        List<NotificationModel> notifications = notificationRepository.findByUserId(userId).stream()
                .map(this::toNotificationModel)
                .collect(Collectors.toList());
        log.info("Retrieved {} available notifications", notifications.size());
        return notifications;
    }

    public Notification createNotification(String userId, String description) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setDescription(description);
        notification.setTimestamp(Instant.now());
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", userId, description);

        NotificationModel notificationModel = toNotificationModel(savedNotification);

        // send notification event via Kafka
        eventProducer.publishNotification(notificationModel);

        return savedNotification;
    }

    private NotificationModel toNotificationModel(Notification notification) {
        return NotificationModel.newBuilder()
                .setNotificationId(notification.getNotificationId())
                .setUserId(notification.getUserId())
                .setDescription(notification.getDescription())
                .setDate(notification.getTimestamp().toEpochMilli())
                .build();
    }
}
