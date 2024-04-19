package com.dynamiceventmanagement.notificationapp.service;

import com.dynamiceventmanagement.notificationapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.notificationapp.model.Group;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GroupApiService {
    private final RestTemplate restTemplate;

    @Value("${api.database.url}")
    private String apiUrl;

    @Value("${api.database.endpoints.groups}")
    private String groupEndpoint;

    public GroupApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Group getOne(String groupId) throws DatabaseApiException {
        String url = apiUrl + groupEndpoint + "/" + groupId;
        ResponseEntity<?> responseEntity = restTemplate.getForEntity(
                url,
                Group.class);

        Object responseBody = responseEntity.getBody();
        if (responseEntity.getStatusCode() != HttpStatus.OK || !(responseBody instanceof Group)) {
            String responseBodyString = (responseBody != null) ? responseBody.toString() : "";
            throw new DatabaseApiException(responseBodyString);
        }

        return (Group) responseEntity.getBody();
    }
}
