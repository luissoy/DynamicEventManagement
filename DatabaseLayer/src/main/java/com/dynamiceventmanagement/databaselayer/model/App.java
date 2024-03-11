package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.AppDto;
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
@Document(collection = "apps")
public class App {
    @Id
    private Long id;

    private String name;

    public App(Long id, AppDto appDto) {
        this.id = id;
        this.name = appDto.getName();
    }
}