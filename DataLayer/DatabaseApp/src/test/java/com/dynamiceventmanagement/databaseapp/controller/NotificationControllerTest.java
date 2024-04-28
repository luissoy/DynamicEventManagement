package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.dto.NotificationDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Notification;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.service.NotificationService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void testGetAll() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Notification notification = new Notification("1",
                "EventId", "appId", null, null, null, null);
        List<Notification> notificationList = List.of(notification);
        Page<Notification> page = new PageImpl<>(notificationList, pageable, notificationList.size());

        PageResponse<Notification> pageResponse = new PageResponse<>(page);

        // When
        when(notificationService.getAll(pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = notificationController.getAll(pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetOne() throws DataNotFoundException {
        // Given
        String id = "1";
        Notification notification = new Notification(id,
                "EventId", "appId", null, null, null, null);

        // When
        when(notificationService.getOne(id)).thenReturn(notification);

        // Then
        ResponseEntity<?> responseEntity = notificationController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(notification, responseEntity.getBody());
    }

    @Test
    void testGetAllByEventId() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String eventId = "eventId";
        Notification notification = new Notification("1",
                eventId, "appId", null, null, null, null);
        List<Notification> notificationList = List.of(notification);
        Page<Notification> page = new PageImpl<>(notificationList, pageable, notificationList.size());

        PageResponse<Notification> pageResponse = new PageResponse<>(page);

        // When
        when(notificationService.getAllByEventId(eventId, pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = notificationController.getAllByEventId(eventId, pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
        assertEquals(eventId,
                ((PageResponse<Notification>) responseEntity.getBody()).getCollection().get(0).getEventId());
    }

    @Test
    void testSave() throws DataIntegrityException {
        // Given
        NotificationDto notificationDto = new NotificationDto("eventId", "appId",
                null, null, null, null);
        Notification notification = new Notification("1", notificationDto);

        // When
        when(notificationService.save(notificationDto)).thenReturn(notification);

        // Then
        ResponseEntity<?> responseEntity = notificationController.save(notificationDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(notification, responseEntity.getBody());
    }
}