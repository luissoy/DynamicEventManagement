package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.bean.GroupDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.bean.UserDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.App;
import com.dynamiceventmanagement.databaselayer.repository.AppRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppServiceTest {

    @Mock
    private AppRepository appRepository;

    @Mock
    private GroupDataDeletionBean groupDataDeletionBean;

    @Mock
    private UserDataDeletionBean userDataDeletionBean;

    @InjectMocks
    private AppService appService;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testGetAll_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        App app = new App("1", "App Name");
        List<App> appList = List.of(app);
        Page<App> expectedPage = new PageImpl<>(appList, pageable, appList.size());

        // When
        when(appRepository.findAll(pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<App> result = appService.getAll(pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
    }

    @Test
    void testGetAll_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<App> appList = List.of();
        Page<App> emptyPage = new PageImpl<>(appList, pageable, appList.size());

        // When
        when(appRepository.findAll(pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<App> result = appService.getAll(pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetOne_WhenAppExists() throws DataNotFoundException {
        // Given
        String appId = "1";
        App expectedApp = new App(appId, "App Name");

        // When
        when(appRepository.findById(appId)).thenReturn(Optional.of(expectedApp));

        // Then
        App resultApp = appService.getOne(appId);
        assertEquals(expectedApp, resultApp);
    }

    @Test
    void testGetOne_WhenAppDoesNotExist() {
        // Given
        String appId = "1";

        // When
        when(appRepository.findById(appId)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            appService.getOne(appId);
        });
    }

    @Test
    void testGetByName_WhenAppExists() throws DataNotFoundException {
        // Given
        String appName = "App Name";
        App expectedApp = new App("1", appName);

        // When
        when(appRepository.findByNotificationUrl(appName)).thenReturn(Optional.of(expectedApp));

        // Then
        App resultApp = appService.getByNotificationUrl(appName);
        assertEquals(expectedApp, resultApp);
    }

    @Test
    void testGetByName_WhenAppDoesNotExist() {
        // Given
        String appName = "App Name";

        // When
        when(appRepository.findByNotificationUrl(appName)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            appService.getByNotificationUrl(appName);
        });
    }

    @Test
    void testSave_WithValidDto() throws DataIntegrityException {
        // Given
        AppDto validDto = new AppDto("App Name");

        // When
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        App savedApp = appService.save(validDto);
        assertNotNull(savedApp);
        assertEquals(validDto.getNotificationUrl(), savedApp.getNotificationUrl());
    }

    @Test
    void testSave_WithEmptyDto() {
        // Given
        AppDto emptyDto = new AppDto();

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            appService.save(emptyDto);
        });
    }

    @Test
    void testUpdate_WithExistingId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";
        AppDto dto = new AppDto("App Name");
        Optional<App> app = Optional.of(new App(id, dto));

        // When
        when(appRepository.findById(id)).thenReturn(app);
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        App updatedApp = appService.update(id, dto);
        assertEquals(id, updatedApp.getId());
        assertEquals(dto.getNotificationUrl(), updatedApp.getNotificationUrl());
    }

    @Test
    void testUpdate_WithNonExistingId() {
        // Given
        String id = "1";
        AppDto dto = new AppDto("App Name");
        Optional<App> app = Optional.empty();

        // When
        when(appRepository.findById(id)).thenReturn(app);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            appService.update(id, dto);
        });
    }

    @Test
    void testDelete_WithExistingId() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        // When
        when(appRepository.existsById(id)).thenReturn(true);
        doNothing().when(userDataDeletionBean).deleteAppParametersFromUsersByAppId(id);

        // Then
        appService.delete(id);
        verify(userDataDeletionBean, times(1)).deleteAppParametersFromUsersByAppId(id);
    }

    @Test
    void testDelete_WithNonExistingId() {
        // Given
        String id = "1";

        // When
        when(appRepository.existsById(id)).thenReturn(false);

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            appService.delete(id);
        });
    }
}
