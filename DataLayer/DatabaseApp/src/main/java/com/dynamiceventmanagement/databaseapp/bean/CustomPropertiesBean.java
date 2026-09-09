package com.dynamiceventmanagement.databaseapp.bean;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:custom.properties")
public class CustomPropertiesBean {

    private static Environment environment;

    public CustomPropertiesBean(Environment env) {
        CustomPropertiesBean.environment = env;
    }

    public static String getProperty(String propertyName) {
        return environment.getProperty(propertyName);
    }
}