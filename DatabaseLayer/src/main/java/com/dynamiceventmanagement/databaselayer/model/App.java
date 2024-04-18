package com.dynamiceventmanagement.databaselayer.model;

import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "apps")
public class App {
    @Id
    private String id;

    @Field("notification_url")
    private String notificationUrl;

    public App(AppDto appDto) {
        this.notificationUrl = appDto.getNotificationUrl();
    }

    public App(String id, AppDto appDto) {
        this.id = id;
        this.notificationUrl = appDto.getNotificationUrl();
    }
}