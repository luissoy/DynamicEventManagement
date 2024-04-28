package com.dynamiceventmanagement.eventapp.service;

import com.dynamiceventmanagement.eventapp.dto.NotificationDto;
import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.model.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationApiService {
    private final RestTemplate restTemplate;

    @Value("${api.database.url}")
    private String apiUrl;

    @Value("${api.database.endpoints.notifications}")
    private String notificationEndpoint;

    public NotificationApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Notification save(NotificationDto notificationDto) throws DatabaseApiException {
        String url = apiUrl + notificationEndpoint;
        ResponseEntity<?> responseEntity = restTemplate.postForEntity(
                url,
                notificationDto,
                Notification.class);

        Object responseBody = responseEntity.getBody();
        if (responseEntity.getStatusCode() != HttpStatus.CREATED || !(responseBody instanceof Notification)) {
            String responseBodyString = (responseBody != null) ? responseBody.toString() : "";
            throw new DatabaseApiException(responseBodyString);
        }

        return (Notification) responseEntity.getBody();
    }
}
