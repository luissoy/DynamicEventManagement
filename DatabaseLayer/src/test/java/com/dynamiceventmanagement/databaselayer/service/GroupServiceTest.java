package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.repository.GroupRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testGetAll_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Group group = new Group("Group Id", "AppId", "GroupName", null);
        List<Group> groupList = List.of(group);
        Page<Group> expectedPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findAll(pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Group> result = groupService.getAll(pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
    }

    @Test
    void testGetAll_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Group> groupList = List.of();
        Page<Group> emptyPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findAll(pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Group> result = groupService.getAll(pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetOne_WhenGroupExists() throws DataNotFoundException {
        // Given
        String groupId = "1";
        Group expectedGroup = new Group(groupId, "AppId", "GroupName", null);

        // When
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(expectedGroup));

        // Then
        Group resultGroup = groupService.getOne(groupId);
        assertEquals(expectedGroup, resultGroup);
    }

    @Test
    void testGetOne_WhenGroupDoesNotExist() {
        // Given
        String groupId = "1";

        // When
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            groupService.getOne(groupId);
        });
    }

    @Test
    void testGetByAppId_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String appId = "1";
        Group group = new Group("1", appId, "GroupName", null);
        List<Group> groupList = List.of(group);
        Page<Group> expectedPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findByAppId(appId, pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Group> result = groupService.getByAppId(appId, pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
        assertEquals(appId, result.getCollection().get(0).getAppId());
    }

    @Test
    void testGetByAppId_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String appId = "1";
        List<Group> groupList = List.of();
        Page<Group> emptyPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findByAppId(appId, pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Group> result = groupService.getByAppId(appId, pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetByAppIdList_NotEmpty() {
        // Given
        String appId = "1";
        Group group = new Group("1", appId, "GroupName", null);
        List<Group> groupList = List.of(group);

        // When
        when(groupRepository.findByAppId(appId)).thenReturn(groupList);

        // Then
        List<Group> result = groupService.getByAppId(appId);
        assertEquals(groupList, result);
        assertEquals(appId, result.get(0).getAppId());
    }

    @Test
    void testGetByAppIdList_Empty() {
        // Given
        String appId = "1";
        List<Group> groupList = List.of();

        // When
        when(groupRepository.findByAppId(appId)).thenReturn(groupList);

        // Then
        List<Group> result = groupService.getByAppId(appId);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testGetByUserId_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String userId = "1";
        List<String> userIds = List.of(userId);
        Group group = new Group("1", "1", "GroupName", userIds);
        List<Group> groupList = List.of(group);
        Page<Group> expectedPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findGroupsByUserId(userId, pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Group> result = groupService.getByUserId(userId, pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
        assertTrue(result.getCollection().get(0).getUserIds().contains(userId));
    }

    @Test
    void testGetByUserId_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String userId = "1";
        List<Group> groupList = List.of();
        Page<Group> emptyPage = new PageImpl<>(groupList, pageable, groupList.size());

        // When
        when(groupRepository.findGroupsByUserId(userId, pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Group> result = groupService.getByUserId(userId, pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetByUserIdList_NotEmpty() {
        // Given
        String userId = "1";
        List<String> userIds = List.of(userId);
        Group group = new Group("1", "1", "GroupName", userIds);
        List<Group> groupList = List.of(group);

        // When
        when(groupRepository.findGroupsByUserId(userId)).thenReturn(groupList);

        // Then
        List<Group> result = groupService.getByUserId(userId);
        assertEquals(groupList, result);
        assertTrue(result.get(0).getUserIds().contains(userId));
    }

    @Test
    void testGetByUserIdList_Empty() {
        // Given
        String userId = "1";
        List<Group> groupList = List.of();

        // When
        when(groupRepository.findGroupsByUserId(userId)).thenReturn(groupList);

        // Then
        List<Group> result = groupService.getByUserId(userId);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testSave_WithValidDto() throws DataIntegrityException {
        // Given
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto validDto = new GroupDto("Appid","GroupName", userIds);

        // When
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        Group savedGroup = groupService.save(validDto);
        assertNotNull(savedGroup);
        assertEquals(validDto.getName(), savedGroup.getName());
    }

    @Test
    void testSave_WithEmptyAppIdDto() {
        // Given
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto emptyDto = new GroupDto(null, "GroupName", userIds);

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            groupService.save(emptyDto);
        });
    }

    @Test
    void testSave_WithEmptyNameDto() {
        // Given
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto emptyDto = new GroupDto("AppId", null, userIds);

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            groupService.save(emptyDto);
        });
    }

    @Test
    void testSave_WithEmptyUserIdsDto() {
        // Given
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto emptyDto = new GroupDto("AppId", "GroupName", null);

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            groupService.save(emptyDto);
        });
    }

    @Test
    void testUpdate_WithExistingId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto dto = new GroupDto("Appid","GroupName", userIds);
        Optional<Group> group = Optional.of(new Group(id, dto));

        // When
        when(groupRepository.findById(id)).thenReturn(group);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        Group updatedGroup = groupService.update(id, dto);
        assertEquals(id, updatedGroup.getId());
        assertEquals(dto.getName(), updatedGroup.getName());
    }

    @Test
    void testUpdate_WithNonExistingId() {
        // Given
        String id = "1";
        String userId = "1";
        List<String> userIds = List.of(userId);
        GroupDto dto = new GroupDto("AppId", "Name", userIds);
        Optional<Group> group = Optional.empty();

        // When
        when(groupRepository.findById(id)).thenReturn(group);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            groupService.update(id, dto);
        });
    }

    @Test
    void testDelete_WithExistingId() throws DataNotFoundException {
        // Given
        String id = "1";

        // When
        when(groupRepository.existsById(id)).thenReturn(true);

        // Then
        groupService.delete(id);
        verify(groupRepository, times(1)).deleteById(id);
    }

    @Test
    void testDelete_WithNonExistingId() {
        // Given
        String id = "1";

        // When
        when(groupRepository.existsById(id)).thenReturn(false);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            groupService.delete(id);
        });
    }
}
