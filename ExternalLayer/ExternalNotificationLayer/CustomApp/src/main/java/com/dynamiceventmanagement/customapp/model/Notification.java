package com.dynamiceventmanagement.customapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;

    private String eventId;

    private String appId;

    private User user;

    private User userToNotify;

    private Group group;

    private Object message;
}
