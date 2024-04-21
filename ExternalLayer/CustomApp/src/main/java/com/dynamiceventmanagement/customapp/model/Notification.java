package com.dynamiceventmanagement.customapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;

    private String eventId;

    private User user;

    private List<User> usersToNotify;

    private Group group;

    private Object message;

}
