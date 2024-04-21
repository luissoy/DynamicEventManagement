package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    @Field("event_id")
    private String eventId;

    private User user;

    @Field("user_to_notify")
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
