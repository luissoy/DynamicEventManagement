package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.EventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "events")
public class Event {
    @Id
    private String id;

    @Field("group_id")
    private String groupId;

    @Field("user_id")
    private String userId;

    @Field("date_time")
    private LocalDateTime dateTime;

    public Event(EventDto eventDto) {
        this.groupId = eventDto.getGroupId();
        this.userId = eventDto.getUserId();
        this.dateTime = eventDto.getDateTime();
    }

    public Event(String id, EventDto eventDto) {
        this.id = id;
        this.groupId = eventDto.getGroupId();
        this.userId = eventDto.getUserId();
        this.dateTime = eventDto.getDateTime();
    }
}
