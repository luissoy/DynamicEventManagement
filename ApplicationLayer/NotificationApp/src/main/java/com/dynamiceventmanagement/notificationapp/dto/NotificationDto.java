package com.dynamiceventmanagement.notificationapp.dto;

import com.dynamiceventmanagement.notificationapp.model.Group;
import com.dynamiceventmanagement.notificationapp.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String eventId;

    private User user;

    private List<User> usersToNotify;

    private Group group;

    private Object message;
}
