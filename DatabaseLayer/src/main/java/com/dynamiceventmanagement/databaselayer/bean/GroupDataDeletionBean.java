package com.dynamiceventmanagement.databaselayer.bean;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupDataDeletionBean {

    @Autowired
    private GroupService groupService;

    public void deleteGroupsByAppId(String appId) throws DataNotFoundException {
        List<Group> groups = groupService.getByAppId(appId);
        for (Group group : groups) {
            groupService.delete(group.getId());
        }
    }

    public void deleteUserFromGroupsByUserId(String userId) throws DataNotFoundException, DataIntegrityException {
        List<Group> groups = groupService.getByUserId(userId);
        for (Group group : groups) {
            group.getUserIds().remove(userId);
            groupService.update(group.getId(), new GroupDto(group));
        }
    }

}