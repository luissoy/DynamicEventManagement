package com.dynamiceventmanagement.customapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GravityApiService {

    private static final int TIMEOUT_MS = 300;
    private static final String NOT_CLASSIFIED = "NO_CLASIFICADA";

    private final RestTemplate restTemplate;

    @Value("${api.gravity.url:http://clasificador-gravedad:8000/api/v1/gravedad}")
    private String gravityUrl;

    public GravityApiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    public String classify(Object message) {
        if (!(message instanceof Map)) {
            return NOT_CLASSIFIED;
        }

        try {
            Map<?, ?> response = restTemplate.postForObject(gravityUrl, message, Map.class);
            Object gravity = response == null ? null : response.get("gravedad");
            return gravity == null ? NOT_CLASSIFIED : gravity.toString();
        } catch (Exception exception) {
            return NOT_CLASSIFIED;
        }
    }
}