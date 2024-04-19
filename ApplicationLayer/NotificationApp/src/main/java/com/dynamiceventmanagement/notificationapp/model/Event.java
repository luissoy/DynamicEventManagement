package com.dynamiceventmanagement.notificationapp.model;

import com.dynamiceventmanagement.notificationapp.dto.EventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String id;

    private String groupId;

    private String userId;

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
