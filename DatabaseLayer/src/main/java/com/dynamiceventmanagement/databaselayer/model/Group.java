package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "groups")
public class Group {
    @Id
    private Long id;

    private Long app_id;

    private String name;

    private List<Long> users;

    public Group(Long id, GroupDto groupsDto) {
        this.id = id;
        this.app_id = groupsDto.getApp_id();
        this.name = groupsDto.getName();
        this.users = groupsDto.getUsers();
    }
}