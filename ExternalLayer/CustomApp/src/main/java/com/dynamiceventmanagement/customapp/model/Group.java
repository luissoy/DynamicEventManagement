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
public class Group {
    private String id;

    private String appId;

    private String name;

    private List<String> userIds;

}