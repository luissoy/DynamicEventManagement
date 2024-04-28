package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.dto.UserDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.User;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.service.UserService;
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
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAll() {
        // Given
        Pageable pageable = Pageable.unpaged();
        User user = new User("1", "User Name", null);
        List<User> userList = List.of(user);
        Page<User> page = new PageImpl<>(userList, pageable, userList.size());

        PageResponse<User> pageResponse = new PageResponse<>(page);

        // When
        when(userService.getAll(pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = userController.getAll(pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetOne() throws DataNotFoundException {
        // Given
        String id = "1";
        User user = new User(id, "User Name", null);

        // When
        when(userService.getOne(id)).thenReturn(user);

        // Then
        ResponseEntity<?> responseEntity = userController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testGetByUsername() throws DataNotFoundException {
        // Given
        String name = "name";
        User user = new User("1", name, null);

        // When
        when(userService.getByUsername(name)).thenReturn(user);

        // Then
        ResponseEntity<?> responseEntity = userController.getByUsername(name);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testGetByAppId() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String appId = "appId";
        User user = new User("1", appId, null);
        List<User> userList = List.of(user);
        Page<User> page = new PageImpl<>(userList, pageable, userList.size());

        PageResponse<User> pageResponse = new PageResponse<>(page);

        // When
        when(userService.getByAppId(appId, pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = userController.getByAppId(appId, pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testSave() throws DataIntegrityException {
        // Given
        UserDto userDto = new UserDto("name", null);
        User user = new User("1", userDto);

        // When
        when(userService.save(userDto)).thenReturn(user);

        // Then
        ResponseEntity<?> responseEntity = userController.save(userDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testUpdate() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        UserDto userDto = new UserDto("name", null);
        User user = new User(id, userDto);

        // When
        when(userService.update(id, userDto)).thenReturn(user);

        // Then
        ResponseEntity<?> responseEntity = userController.update(id, userDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testDelete() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        // When
        doNothing().when(userService).delete(id);

        // Then
        ResponseEntity<?> responseEntity = userController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}
