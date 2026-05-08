package com.bank.app.notification.controller;

import com.bank.app.common.api.ApiResponse;
import com.bank.app.notification.service.NotificationService;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','ADMIN')")
    public ApiResponse<Void> send(@RequestParam @NonNull String recipient,
                                  @RequestParam @NonNull String subject,
                                  @RequestParam @NonNull String body,
                                  @RequestParam(defaultValue = "EMAIL") @NonNull String channel) {
        notificationService.publishNotification(recipient, subject, body, channel);
        return ApiResponse.success("Notification queued");
    }
}
