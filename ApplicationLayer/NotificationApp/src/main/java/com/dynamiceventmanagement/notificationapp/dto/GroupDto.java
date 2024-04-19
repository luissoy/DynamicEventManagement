package com.dynamiceventmanagement.notificationapp.dto;

import com.dynamiceventmanagement.notificationapp.model.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    private String appId;

    private String name;

    private List<String> userIds;

    public GroupDto(Group group) {
        this.appId = group.getAppId();
        this.name = group.getName();
        this.userIds = group.getUserIds();
    }
}