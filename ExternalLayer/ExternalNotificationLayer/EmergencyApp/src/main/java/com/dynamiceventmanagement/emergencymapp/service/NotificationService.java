package com.dynamiceventmanagement.emergencymapp.service;

import com.dynamiceventmanagement.emergencymapp.model.Notification;
import com.dynamiceventmanagement.emergencymapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailApiService emailApiService;
    public void save (Notification notification) {
        String appId = notification.getAppId();
        User userToNotify = notification.getUserToNotify();

        String to = getEmail(
                userToNotify,
                appId
        );

        String subject = getSubject(
                userToNotify,
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
                "New Emergency from group " + groupName :
                subject + ": New Emergency from group " + groupName;
    }

    private String getText(User user, Object message) {
        return "This is a emergency notification from " +
                user.getUsername() +
                "\n\n" +
                "The message is: " +
                message.toString();
    }
}
