package com.dynamiceventmanagement.databaselayer.bean;

import com.dynamiceventmanagement.databaselayer.dto.UserDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.User;
import com.dynamiceventmanagement.databaselayer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDataDeletionBean {

    @Autowired
    private UserService userService;

    public void deleteAppParametersFromUsersByAppId(String appId) throws DataNotFoundException, DataIntegrityException {
        List<User> users = userService.getByAppId(appId);
        for (User user : users) {
            user.getAppParameters().remove(appId);
            userService.update(user.getId(), new UserDto(user));
        }
    }

}