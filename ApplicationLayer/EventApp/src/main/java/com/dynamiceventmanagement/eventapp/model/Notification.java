package com.dynamiceventmanagement.eventapp.model;

import com.dynamiceventmanagement.eventapp.dto.NotificationDto;
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

    public Notification(NotificationDto eventDto) {
        this.eventId = eventDto.getEventId();
        this.appId = eventDto.getAppId();
        this.user = eventDto.getUser();
        this.userToNotify = eventDto.getUserToNotify();
        this.group = eventDto.getGroup();
        this.message = eventDto.getMessage();
    }

    public Notification(String id, NotificationDto eventDto) {
        this.id = id;
        this.eventId = eventDto.getEventId();
        this.appId = eventDto.getAppId();
        this.user = eventDto.getUser();
        this.userToNotify = eventDto.getUserToNotify();
        this.group = eventDto.getGroup();
        this.message = eventDto.getMessage();
    }
}
