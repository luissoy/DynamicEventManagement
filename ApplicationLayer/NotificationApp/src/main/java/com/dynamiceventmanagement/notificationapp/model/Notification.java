package com.dynamiceventmanagement.notificationapp.model;

import com.dynamiceventmanagement.notificationapp.dto.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;

    private String eventId;

    private User user;

    private List<User> usersToNotify;

    private Group group;

    private Object message;

    public Notification(NotificationDto eventDto) {
        this.eventId = eventDto.getEventId();
        this.user = eventDto.getUser();
        this.usersToNotify = eventDto.getUsersToNotify();
        this.group = eventDto.getGroup();
        this.message = eventDto.getMessage();
    }

    public Notification(String id, NotificationDto eventDto) {
        this.id = id;
        this.eventId = eventDto.getEventId();
        this.user = eventDto.getUser();
        this.usersToNotify = eventDto.getUsersToNotify();
        this.group = eventDto.getGroup();
        this.message = eventDto.getMessage();
    }
}
