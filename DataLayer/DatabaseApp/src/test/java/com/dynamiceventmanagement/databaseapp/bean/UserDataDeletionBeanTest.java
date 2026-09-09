package com.dynamiceventmanagement.databaseapp.bean;

import com.dynamiceventmanagement.databaseapp.dto.UserDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.User;
import com.dynamiceventmanagement.databaseapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDataDeletionBeanTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDataDeletionBean userDataDeletionBean;

    @Test
    void testDeleteAppParametersFromUsersByAppId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String appId = "AppId";
        String userId1 = "userId1";
        String userId2 = "userId2";

        List<User> users = new ArrayList<>();
        Map<String, Map<String, String>> appParameters = new HashMap<>();
        appParameters.put(appId, new HashMap<>(Map.of("appId", "value")));

        User user1 = new User(userId1, "Username1", appParameters);
        users.add(user1);

        User user2 = new User(userId2, "Username2", appParameters);
        users.add(user2);

        // When
        when(userService.getByAppId(appId)).thenReturn(users);
        when(userService.update(any(), any())).thenReturn(null);

        // Then
        userDataDeletionBean.deleteAppParametersFromUsersByAppId(appId);

        verify(userService, times(1)).getByAppId(appId);
        verify(userService, times(1)).update(eq(userId1), any(UserDto.class));
        verify(userService, times(1)).update(eq(userId2), any(UserDto.class));
    }
}
