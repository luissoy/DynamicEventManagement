package com.dynamiceventmanagement.eventapp.dto;

import com.dynamiceventmanagement.eventapp.model.Group;
import com.dynamiceventmanagement.eventapp.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String eventId;

    private String appId;

    private User user;

    private User userToNotify;

    private Group group;

    private Object message;
}
