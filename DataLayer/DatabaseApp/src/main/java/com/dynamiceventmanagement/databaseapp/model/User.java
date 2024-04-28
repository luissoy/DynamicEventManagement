package com.dynamiceventmanagement.databaseapp.model;

import com.dynamiceventmanagement.databaseapp.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String username;

    @Field("apps_parameters")
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