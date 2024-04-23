package com.dynamiceventmanagement.notificationapp.service;

import com.dynamiceventmanagement.notificationapp.dto.NotificationDto;
import com.dynamiceventmanagement.notificationapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.notificationapp.model.Group;
import com.dynamiceventmanagement.notificationapp.model.Notification;
import com.dynamiceventmanagement.notificationapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private UserApiService userApiService;

    @Autowired
    private GroupApiService groupApiService;

    @Autowired
    private NotificationApiService notificationApiService;

    @Autowired
    private ExternalLayerApplicationsApiService externalLayerApplicationsApiService;

    public void sendNotifications(String eventId, String userId, String groupId, String appId, Object message) throws DatabaseApiException {
        Group group = groupApiService.getOne(groupId);
        List<String> userIds = group.getUserIds();
        User user = getUserCustomApp(userId, appId);

        for (String userIdToNotify : userIds) {
            NotificationDto notificationDto = new NotificationDto(
                    eventId,
                    user,
                    getUserCustomApp(userIdToNotify, appId),
                    group,
                    message
            );

            Notification notification = notificationApiService.save(notificationDto);

            externalLayerApplicationsApiService.sendNotification(notification, appId);
        }

    }

    private User getUserCustomApp (String userId, String appId) throws DatabaseApiException {
        User user = userApiService.getOne(userId);

        Map<String, String> appParameterCustomApp = user.getAppParameters().get(appId);
        Map<String, Map<String, String>> appParametersCustomApp = new HashMap<>();

        if (appParameterCustomApp != null) {
            appParametersCustomApp.put(appId, appParameterCustomApp);
        }

        user.setAppParameters(appParametersCustomApp);

        return user;
    }
}
