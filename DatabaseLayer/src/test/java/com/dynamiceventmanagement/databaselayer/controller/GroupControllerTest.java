package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.service.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @Test
    void testGetAll() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Group group = new Group("GroupId", "Group Name", null);
        List<Group> groupList = List.of(group);
        Page<Group> page = new PageImpl<>(groupList, pageable, groupList.size());

        PageResponse<Group> pageResponse = new PageResponse<>(page);

        // When
        when(groupService.getAll(pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = groupController.getAll(pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetOne() throws DataNotFoundException {
        // Given
        String id = "1";
        Group group = new Group(id, "Group Name", null);

        // When
        when(groupService.getOne(id)).thenReturn(group);

        // Then
        ResponseEntity<?> responseEntity = groupController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(group, responseEntity.getBody());
    }

    @Test
    void testGetByAppId() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String name = "Group Name";
        Group group = new Group("GroupId", name, null);
        List<Group> groupList = List.of(group);
        Page<Group> page = new PageImpl<>(groupList, pageable, groupList.size());

        PageResponse<Group> pageResponse = new PageResponse<>(page);

        // When
        when(groupService.getByName(name, pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = groupController.getByName(name, pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetByUserId() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String userId = "UserId";
        List<String> userIds = List.of(userId);

        Group group = new Group("GroupId", "Group Name", userIds);
        List<Group> groupList = List.of(group);
        Page<Group> page = new PageImpl<>(groupList, pageable, groupList.size());

        PageResponse<Group> pageResponse = new PageResponse<>(page);

        // When
        when(groupService.getByUserId(userId, pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = groupController.getByUserId(userId, pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testSave() throws DataIntegrityException {
        // Given
        GroupDto groupDto = new GroupDto("Name", null);
        Group group = new Group("1", groupDto);

        // When
        when(groupService.save(groupDto)).thenReturn(group);

        // Then
        ResponseEntity<?> responseEntity = groupController.save(groupDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(group, responseEntity.getBody());
    }

    @Test
    void testUpdate() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        GroupDto groupDto = new GroupDto("Name", null);
        Group group = new Group(id, groupDto);

        // When
        when(groupService.update(id, groupDto)).thenReturn(group);

        // Then
        ResponseEntity<?> responseEntity = groupController.update(id, groupDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(group, responseEntity.getBody());
    }

    @Test
    void testDelete() throws DataNotFoundException {
        // Given
        String id = "1";

        // When
        doNothing().when(groupService).delete(id);

        // Then
        ResponseEntity<?> responseEntity = groupController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}
