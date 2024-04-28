package com.dynamiceventmanagement.eventapp.service;

import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.model.App;
import com.dynamiceventmanagement.eventapp.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalLayerApplicationsApiService {

    @Autowired
    private AppApiService appApiService;

    private final RestTemplate restTemplate;


    public ExternalLayerApplicationsApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void sendNotification(Notification notification, String appId) throws DatabaseApiException {
        App app = appApiService.getOne(appId);
        String url = app.getNotificationUrl();

        restTemplate.postForObject(
                url,
                notification,
                Void.class);
    }
}
