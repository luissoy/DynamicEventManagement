package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.bean.GroupDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.dto.UserDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.User;
import com.dynamiceventmanagement.databaselayer.repository.UserRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupDataDeletionBean groupDataDeletionBean;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testGetAll_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        User user = new User("1", "Username", null);
        List<User> userList = List.of(user);
        Page<User> expectedPage = new PageImpl<>(userList, pageable, userList.size());

        // When
        when(userRepository.findAll(pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<User> result = userService.getAll(pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
    }

    @Test
    void testGetAll_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<User> userList = List.of();
        Page<User> emptyPage = new PageImpl<>(userList, pageable, userList.size());

        // When
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<User> result = userService.getAll(pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetOne_WhenUserExists() throws DataNotFoundException {
        // Given
        String userId = "1";
        User expectedUser = new User(userId, "Username", null);

        // When
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Then
        User resultUser = userService.getOne(userId);
        assertEquals(expectedUser, resultUser);
    }

    @Test
    void testGetOne_WhenUserDoesNotExist() {
        // Given
        String userId = "1";

        // When
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            userService.getOne(userId);
        });
    }

    @Test
    void testGetByUsername_WhenUserExists() throws DataNotFoundException {
        // Given
        String userName = "Username";
        User expectedUser = new User("1", userName, null);

        // When
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(expectedUser));

        // Then
        User resultUser = userService.getByUsername(userName);
        assertEquals(expectedUser, resultUser);
    }

    @Test
    void testGetByUsername_WhenUserDoesNotExist() {
        // Given
        String userName = "Username";

        // When
        when(userRepository.findByUsername(userName)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            userService.getByUsername(userName);
        });
    }

    @Test
    void testGetByAppId_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String appId = "1";
        Map<String, Map<String, String>> appParameters = new HashMap<>();
        appParameters.put(appId, new HashMap<>(Map.of("key", "value")));
        User user = new User("1", "Username", appParameters);
        List<User> userList = List.of(user);
        Page<User> expectedPage = new PageImpl<>(userList, pageable, userList.size());

        // When
        when(userRepository.findUsersWithByAppId(appId, pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<User> result = userService.getByAppId(appId, pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
        assertFalse(result.getCollection().get(0).getAppParameters().get(appId).isEmpty());
    }

    @Test
    void testGetByAppId_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String appId = "1";
        List<User> userList = List.of();
        Page<User> emptyPage = new PageImpl<>(userList, pageable, userList.size());

        // When
        when(userRepository.findUsersWithByAppId(appId, pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<User> result = userService.getByAppId(appId, pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetByAppIdList_NotEmpty() {
        // Given
        String appId = "1";
        Map<String, Map<String, String>> appParameters = new HashMap<>();
        appParameters.put(appId, new HashMap<>(Map.of("key", "value")));
        User user = new User("1", "Username", appParameters);
        List<User> userList = List.of(user);

        // When
        when(userRepository.findUsersWithByAppId(appId)).thenReturn(userList);

        // Then
        List<User> result = userService.getByAppId(appId);
        assertEquals(userList, result);
        assertFalse(result.get(0).getAppParameters().get(appId).isEmpty());
    }

    @Test
    void testGetByAppIdList_Empty() {
        // Given
        String appId = "1";
        List<User> userList = List.of();

        // When
        when(userRepository.findUsersWithByAppId(appId)).thenReturn(userList);

        // Then
        List<User> result = userService.getByAppId(appId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_WithValidDto() throws DataIntegrityException {
        // Given
        UserDto validDto = new UserDto("Username", null);

        // When
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        User savedUser = userService.save(validDto);
        assertNotNull(savedUser);
        assertEquals(validDto.getUsername(), savedUser.getUsername());
    }

    @Test
    void testSave_WithEmptyDto() {
        // Given
        UserDto emptyDto = new UserDto();

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            userService.save(emptyDto);
        });
    }

    @Test
    void testUpdate_WithExistingId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";
        UserDto dto = new UserDto("Username", null);
        Optional<User> user = Optional.of(new User(id, dto));

        // When
        when(userRepository.findById(id)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        User updatedUser = userService.update(id, dto);
        assertEquals(id, updatedUser.getId());
        assertEquals(dto.getUsername(), updatedUser.getUsername());
    }

    @Test
    void testUpdate_WithExistingIdAndDifferentUsername() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";
        UserDto dto = new UserDto("Username", null);
        Optional<User> user = Optional.of(new User(id, dto));
        user.get().setUsername("oldUsername");

        // When
        when(userRepository.findById(id)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        User updatedUser = userService.update(id, dto);
        assertEquals(id, updatedUser.getId());
        assertEquals(dto.getUsername(), updatedUser.getUsername());
    }

    @Test
    void testUpdate_WithNonExistingId() {
        // Given
        String id = "1";
        UserDto emptyDto = new UserDto();
        Optional<User> user = Optional.empty();

        // When
        when(userRepository.findById(id)).thenReturn(user);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            userService.update(id, emptyDto);
        });
    }

    @Test
    void testDelete_WithExistingId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        // When
        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(groupDataDeletionBean).deleteUserFromGroupsByUserId(id);

        // Then
        userService.delete(id);
        verify(groupDataDeletionBean, times(1)).deleteUserFromGroupsByUserId(id);
    }

    @Test
    void testDelete_WithNonExistingId() {
        // Given
        String id = "1";

        // When
        when(userRepository.existsById(id)).thenReturn(false);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            userService.delete(id);
        });
    }
}
