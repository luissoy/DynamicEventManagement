package com.dynamiceventmanagement.databaseapp.bean;

import com.dynamiceventmanagement.databaseapp.dto.GroupDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Group;
import com.dynamiceventmanagement.databaseapp.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupDataDeletionBean {

    @Autowired
    private GroupService groupService;

    public void deleteUserFromGroupsByUserId(String userId) throws DataNotFoundException, DataIntegrityException {
        List<Group> groups = groupService.getByUserId(userId);
        for (Group group : groups) {
            group.getUserIds().remove(userId);
            groupService.update(group.getId(), new GroupDto(group));
        }
    }

}