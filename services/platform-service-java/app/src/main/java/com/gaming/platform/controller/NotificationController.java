package com.gaming.platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.NotificationModel;
import com.gaming.platform.service.NotificationsService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationsService notificationsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationModel>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationsService.getUserNotifications(userId));
    }

    @PostMapping("/new/user/{userId}/description/{description}")
    public ResponseEntity<Void> createNotification(@PathVariable String userId, @PathVariable String description) {
        if (notificationsService.createNotification(userId, description) == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    


    
}
