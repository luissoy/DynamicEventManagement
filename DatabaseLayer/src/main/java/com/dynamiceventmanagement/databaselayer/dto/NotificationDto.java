package com.dynamiceventmanagement.databaselayer.dto;

import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.model.User;
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

    private User user;

    private User userToNotify;

    private Group group;

    private Object message;
}
