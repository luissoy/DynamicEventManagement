package com.dynamiceventmanagement.eventapp.model;

import com.dynamiceventmanagement.eventapp.dto.GroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private String id;

    private String name;

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