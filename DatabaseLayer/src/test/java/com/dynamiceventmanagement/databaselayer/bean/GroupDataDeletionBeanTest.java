package com.dynamiceventmanagement.databaselayer.bean;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.service.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupDataDeletionBeanTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupDataDeletionBean groupDataDeletionBean;

    @Test
    void testDeleteUserFromGroupsByUserId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String appId = "AppId";
        String userId = "userId1";
        List<String> userIds = new ArrayList<>(List.of(userId));
        List<Group> groups = new ArrayList<>();
        groups.add(new Group("1", "name1", userIds));
        groups.add(new Group("2", "name2", userIds));

        // When
        when(groupService.getByUserId(userId)).thenReturn(groups);
        when(groupService.update(any(), any())).thenReturn(null);

        // Then
        groupDataDeletionBean.deleteUserFromGroupsByUserId(userId);

        verify(groupService, times(1)).getByUserId(userId);
        verify(groupService, times(1)).update(eq("1"), any(GroupDto.class));
        verify(groupService, times(1)).update(eq("2"), any(GroupDto.class));
    }
}
