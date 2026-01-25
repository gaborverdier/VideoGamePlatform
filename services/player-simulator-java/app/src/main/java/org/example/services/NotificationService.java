package org.example.services;

import java.util.ArrayList;
import java.util.List;

import org.example.models.Notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaming.api.models.NotificationModel;

public class NotificationService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<NotificationModel> fetchUserNotifications(String userId) {
        List<NotificationModel> notificationModels = new ArrayList<>();
        try {
            PlatformApiClient apiClient = new PlatformApiClient();
            String json = apiClient.getUserNotificationsJson(userId); // You must implement getUserNotificationsJson in PlatformApiClient
            notificationModels = objectMapper.readValue(json,
                    new TypeReference<List<NotificationModel>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notificationModels;
    }

}
