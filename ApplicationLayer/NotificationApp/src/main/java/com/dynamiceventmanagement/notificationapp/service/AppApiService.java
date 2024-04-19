package com.dynamiceventmanagement.notificationapp.service;

import com.dynamiceventmanagement.notificationapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.notificationapp.model.App;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AppApiService {
    private final RestTemplate restTemplate;

    @Value("${api.database.url}")
    private String apiUrl;

    @Value("${api.database.endpoints.apps}")
    private String appEndpoint;

    public AppApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public App getOne(String appId) throws DatabaseApiException {
        String url = apiUrl + appEndpoint + "/" + appId;
        ResponseEntity<?> responseEntity = restTemplate.getForEntity(
                url,
                App.class);

        Object responseBody = responseEntity.getBody();
        if (responseEntity.getStatusCode() != HttpStatus.OK || !(responseBody instanceof App)) {
            String responseBodyString = (responseBody != null) ? responseBody.toString() : "";
            throw new DatabaseApiException(responseBodyString);
        }

        return (App) responseEntity.getBody();
    }
}
