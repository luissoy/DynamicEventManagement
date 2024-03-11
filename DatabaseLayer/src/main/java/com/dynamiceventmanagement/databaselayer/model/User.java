package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private Long id;

    private String username;

    private Map<Long, Map<String, String>> parameters;

    public User(Long id, UserDto userDto) {
        this.id = id;
        this.username = userDto.getUsername();
        this.parameters = userDto.getParameters();
    }
}