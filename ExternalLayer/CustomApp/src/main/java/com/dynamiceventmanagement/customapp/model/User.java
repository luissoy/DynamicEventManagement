package com.dynamiceventmanagement.customapp.model;

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

}