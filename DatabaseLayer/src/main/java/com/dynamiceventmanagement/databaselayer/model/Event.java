package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.EventDto;
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
@Document(collection = "events")
public class Event {
    @Id
    private String id;

    @Field("group_id")
    private String groupId;

    @Field("group_info")
    private Group groupInfo;

    @Field("user_id")
    private String userId;

    public Event(EventDto eventDto) {
        this.groupId = eventDto.getGroupId();
        this.groupInfo = eventDto.getGroupInfo();
        this.userId = eventDto.getUserId();
    }

    public Event(String id, EventDto eventDto) {
        this.id = id;
        this.groupId = eventDto.getGroupId();
        this.groupInfo = eventDto.getGroupInfo();
        this.userId = eventDto.getUserId();
    }
}
