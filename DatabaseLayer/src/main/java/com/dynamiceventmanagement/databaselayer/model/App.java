package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "apps")
public class App {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    public App(AppDto appDto) {
        this.name = appDto.getName();
    }

    public App(String id, AppDto appDto) {
        this.id = id;
        this.name = appDto.getName();
    }
}