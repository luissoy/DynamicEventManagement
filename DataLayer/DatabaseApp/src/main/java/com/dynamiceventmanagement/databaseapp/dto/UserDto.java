package com.dynamiceventmanagement.databaseapp.dto;

import com.dynamiceventmanagement.databaseapp.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String username;

    private Map<String, Map<String, String>> appParameters;

    public UserDto(User user) {
        this.username = user.getUsername();
        this.appParameters = user.getAppParameters();
    }
}