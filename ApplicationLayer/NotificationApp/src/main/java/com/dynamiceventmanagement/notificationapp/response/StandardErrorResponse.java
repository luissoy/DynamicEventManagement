package com.dynamiceventmanagement.notificationapp.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StandardErrorResponse {
    private final String message;
    private final String type;

    public StandardErrorResponse(String message, String type) {
        this.message = message;
        this.type = type;
    }
}
