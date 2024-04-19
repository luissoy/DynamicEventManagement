package com.dynamiceventmanagement.customapp.service;

import com.dynamiceventmanagement.customapp.model.Notification;
import com.dynamiceventmanagement.customapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailApiService emailApiService;
    public void save (Notification notification) {
        String appId = notification.getGroup().getAppId();

        String to = getEmail(
                notification.getUserToNotify(),
                appId
        );

        String subject = getSubject(
                notification.getUserToNotify(),
                notification.getGroup().getName(),
                appId
        );

        String text = getText(
                notification.getUser(),
                notification.getMessage()
        );

        emailApiService.sendEmail(to, subject, text);
    }

    private String getEmail(User user, String appId) {
        return user.getAppParameters().get(appId).get("email");
    }

    private String getSubject(User user, String groupName, String appId) {
        String subject = user.getAppParameters().get(appId).get("subject_title");
        return subject == null ?
                "New Notification: " + groupName :
                subject + ": " + groupName;
    }

    private String getText(User user, Object message) {
        return "This is a notification from " +
                user.getUsername() +
                "\n\n" +
                message.toString();
    }
}
