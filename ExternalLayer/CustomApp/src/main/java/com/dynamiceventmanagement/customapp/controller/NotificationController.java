package com.dynamiceventmanagement.customapp.controller;

import com.dynamiceventmanagement.customapp.exception.ExternalDataIntegrityException;
import com.dynamiceventmanagement.customapp.model.Notification;
import com.dynamiceventmanagement.customapp.service.NotificationService;
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
            @RequestBody Notification notification)
            throws ExternalDataIntegrityException {
        notificationService.save(notification);
    }
}
