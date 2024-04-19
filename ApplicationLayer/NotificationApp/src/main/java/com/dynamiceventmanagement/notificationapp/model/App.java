package com.dynamiceventmanagement.notificationapp.model;

import com.dynamiceventmanagement.notificationapp.dto.AppDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class App {
    private String id;

    private String notificationUrl;

    public App(AppDto appDto) {
        this.notificationUrl = appDto.getNotificationUrl();
    }

    public App(String id, AppDto appDto) {
        this.id = id;
        this.notificationUrl = appDto.getNotificationUrl();
    }
}