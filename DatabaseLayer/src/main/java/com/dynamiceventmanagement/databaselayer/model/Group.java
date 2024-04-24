package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
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
@Document(collection = "groups")
public class Group {
    @Id
    private String id;

    private String name;

    @Field("user_ids")
    private List<String> userIds;

    public Group(GroupDto groupsDto) {
        this.name = groupsDto.getName();
        this.userIds = groupsDto.getUserIds();
    }

    public Group(String id, GroupDto groupsDto) {
        this.id = id;
        this.name = groupsDto.getName();
        this.userIds = groupsDto.getUserIds();
    }
}