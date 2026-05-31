package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.Notification;
import com.example.financebackend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestParam Integer userId,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        try {
            List<Notification> notifications;
            if (unreadOnly) {
                notifications = notificationService.getUnreadNotifications(userId);
            } else {
                notifications = notificationService.getNotificationsByUserId(userId);
            }
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Integer id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
