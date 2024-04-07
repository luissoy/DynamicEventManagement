package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("user_id")
    private String userId;

    @Field("notification_info")
    private String notificationInfo;

    public Notification(NotificationDto eventDto) {
        this.eventId = eventDto.getEventId();
        this.notificationInfo = eventDto.getNotificationInfo();
    }

    public Notification(String id, NotificationDto eventDto) {
        this.id = id;
        this.eventId = eventDto.getEventId();
        this.notificationInfo = eventDto.getNotificationInfo();
    }
}
