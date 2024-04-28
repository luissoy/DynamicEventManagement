package com.dynamiceventmanagement.eventapp.service;

import com.dynamiceventmanagement.eventapp.dto.EventDto;
import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EventApiService {
    private final RestTemplate restTemplate;

    @Value("${api.database.url}")
    private String apiUrl;

    @Value("${api.database.endpoints.events}")
    private String eventEndpoint;

    public EventApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Event save(EventDto eventDto) throws DatabaseApiException {
        String url = apiUrl + eventEndpoint;
        ResponseEntity<?> responseEntity = restTemplate.postForEntity(
                url,
                eventDto,
                Event.class);

        Object responseBody = responseEntity.getBody();
        if (responseEntity.getStatusCode() != HttpStatus.CREATED || !(responseBody instanceof Event)) {
            String responseBodyString = (responseBody != null) ? responseBody.toString() : "";
            throw new DatabaseApiException(responseBodyString);
        }

        return (Event) responseEntity.getBody();
    }
}
