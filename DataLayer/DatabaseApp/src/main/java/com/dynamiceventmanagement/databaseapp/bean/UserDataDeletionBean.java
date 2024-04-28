package com.dynamiceventmanagement.databaseapp.bean;

import com.dynamiceventmanagement.databaseapp.dto.UserDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.User;
import com.dynamiceventmanagement.databaseapp.service.UserService;
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