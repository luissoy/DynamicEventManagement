package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.dto.AppDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.App;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.service.AppService;
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
class AppControllerTest {

    @Mock
    private AppService appService;

    @InjectMocks
    private AppController appController;

    @Test
    void testGetAll() {
        // Given
        Pageable pageable = Pageable.unpaged();
        App app = new App("1", "App Name");
        List<App> appList = List.of(app);
        Page<App> page = new PageImpl<>(appList, pageable, appList.size());

        PageResponse<App> pageResponse = new PageResponse<>(page);

        // When
        when(appService.getAll(pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = appController.getAll(pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetOne() throws DataNotFoundException {
        // Given
        String id = "1";
        App app = new App(id, "App Name");

        // When
        when(appService.getOne(id)).thenReturn(app);

        // Then
        ResponseEntity<?> responseEntity = appController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(app, responseEntity.getBody());
    }

    @Test
    void testGetByName() throws DataNotFoundException {
        // Given
        String name = "name";
        App app = new App("1", name);

        // When
        when(appService.getByNotificationUrl(name)).thenReturn(app);

        // Then
        ResponseEntity<?> responseEntity = appController.getByNotificationUrl(name);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(app, responseEntity.getBody());
    }

    @Test
    void testSave() throws DataIntegrityException {
        // Given
        AppDto appDto = new AppDto("name");
        App app = new App("1", appDto);

        // When
        when(appService.save(appDto)).thenReturn(app);

        // Then
        ResponseEntity<?> responseEntity = appController.save(appDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(app, responseEntity.getBody());
    }

    @Test
    void testUpdate() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        AppDto appDto = new AppDto("name");
        App app = new App(id, appDto);

        // When
        when(appService.update(id, appDto)).thenReturn(app);

        // Then
        ResponseEntity<?> responseEntity = appController.update(id, appDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(app, responseEntity.getBody());
    }

    @Test
    void testDelete() throws DataNotFoundException, DataIntegrityException {
        // Given
        String id = "1";

        // When
        doNothing().when(appService).delete(id);

        // Then
        ResponseEntity<?> responseEntity = appController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}
