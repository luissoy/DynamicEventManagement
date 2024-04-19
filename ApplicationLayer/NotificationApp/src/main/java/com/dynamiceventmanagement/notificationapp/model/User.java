package com.dynamiceventmanagement.notificationapp.model;

import com.dynamiceventmanagement.notificationapp.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;

    private String username;

    private Map<String, Map<String, String>> appParameters;

    public User(UserDto userDto) {
        this.username = userDto.getUsername();
        this.appParameters = userDto.getAppParameters();
    }

    public User(String id, UserDto userDto) {
        this.id = id;
        this.username = userDto.getUsername();
        this.appParameters = userDto.getAppParameters();
    }
}