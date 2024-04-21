package com.dynamiceventmanagement.customapp.service;

import com.dynamiceventmanagement.customapp.exception.ExternalDataIntegrityException;
import com.dynamiceventmanagement.customapp.model.Notification;
import com.dynamiceventmanagement.customapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailApiService emailApiService;
    public void save (Notification notification) throws ExternalDataIntegrityException {
        validateDataIntegrity(notification);

        String appId = notification.getGroup().getAppId();
        User userToNotify = notification.getUsersToNotify().get(0);

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
                "New Notification: " + groupName :
                subject + ": " + groupName;
    }

    private String getText(User user, Object message) {
        return "This is a notification from " +
                user.getUsername() +
                "\n\n" +
                message.toString();
    }

    private void validateDataIntegrity (Notification notification) throws ExternalDataIntegrityException {
        if (notification.getUsersToNotify().size() != 1) {
            throw new ExternalDataIntegrityException();
        }
    }
}
