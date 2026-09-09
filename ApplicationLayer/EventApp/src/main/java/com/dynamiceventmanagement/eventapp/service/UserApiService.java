package com.dynamiceventmanagement.eventapp.service;

import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserApiService {
    private final RestTemplate restTemplate;

    @Value("${api.database.url}")
    private String apiUrl;

    @Value("${api.database.endpoints.users}")
    private String userEndpoint;

    public UserApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User getOne(String userId) throws DatabaseApiException {
        String url = apiUrl + userEndpoint + "/" + userId;
        ResponseEntity<?> responseEntity = restTemplate.getForEntity(
                url,
                User.class);

        Object responseBody = responseEntity.getBody();
        if (responseEntity.getStatusCode() != HttpStatus.OK || !(responseBody instanceof User)) {
            String responseBodyString = (responseBody != null) ? responseBody.toString() : "";
            throw new DatabaseApiException(responseBodyString);
        }

        return (User) responseEntity.getBody();
    }
}
