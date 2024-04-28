package com.dynamiceventmanagement.emergencymapp.controller;

import com.dynamiceventmanagement.emergencymapp.model.Notification;
import com.dynamiceventmanagement.emergencymapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Async
    @PostMapping
    public void save(
            @RequestBody Notification notification
    ) {
        notificationService.save(notification);
    }
}
