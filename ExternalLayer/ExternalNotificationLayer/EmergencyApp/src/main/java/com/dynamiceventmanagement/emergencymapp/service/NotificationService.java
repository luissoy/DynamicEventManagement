package com.dynamiceventmanagement.emergencyapp.service;

import com.dynamiceventmanagement.emergencyapp.model.Notification;
import com.dynamiceventmanagement.emergencyapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private EmailApiService emailApiService;

    @Autowired
    private GravityApiService gravityApiService;

    public void save (Notification notification) {
        String appId = notification.getAppId();
        User userToNotify = notification.getUserToNotify();
        Object message = notification.getMessage();

        String to = getEmail(
                userToNotify,
                appId
        );

        String subject = getSubject(
                userToNotify,
                notification.getGroup().getName(),
                appId
        );

        String gravity = gravityApiService.classify(message);

        String text = getText(
                notification.getUser(),
                message,
                gravity
        );

        emailApiService.sendEmail(to, subject, text);
    }

    private String getEmail(User user, String appId) {
        return user.getAppParameters().get(appId).get("email");
    }

    private String getSubject(User user, String groupName, String appId) {
        String subject = user.getAppParameters().get(appId).get("subject_title");
        return subject == null ?
                "New Emergency from group " + groupName :
                subject + ": New Emergency from group " + groupName;
    }

    private String getText(User user, Object message, String gravity) {
        return "This is a emergency notification from " +
                user.getUsername() +
                "\n\n" +
                "Severity: " +
                gravity +
                "\n\n" +
                "The message is: " +
                getMessageText(message);
    }

    private String getMessageText(Object message) {
        if (message instanceof Map<?, ?> map && map.get("message") != null) {
            return map.get("message").toString();
        }
        return message.toString();
    }
}