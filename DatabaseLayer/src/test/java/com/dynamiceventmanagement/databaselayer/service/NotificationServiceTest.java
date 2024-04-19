package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.NotificationDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Notification;
import com.dynamiceventmanagement.databaselayer.model.User;
import com.dynamiceventmanagement.databaselayer.repository.NotificationRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testGetAll_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Notification notification = new Notification("1",
                "eventId", null, null, null, null);
        List<Notification> notificationList = List.of(notification);
        Page<Notification> expectedPage = new PageImpl<>(notificationList, pageable, notificationList.size());

        // When
        when(notificationRepository.findAll(pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Notification> result = notificationService.getAll(pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
    }

    @Test
    void testGetAll_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Notification> notificationList = List.of();
        Page<Notification> emptyPage = new PageImpl<>(notificationList, pageable, notificationList.size());

        // When
        when(notificationRepository.findAll(pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Notification> result = notificationService.getAll(pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetOne_WhenNotificationExists() throws DataNotFoundException {
        // Given
        String notificationId = "1";
        Notification expectedNotification = new Notification(notificationId,
                "EventId", null, null, null, null);

        // When
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(expectedNotification));

        // Then
        Notification resultNotification = notificationService.getOne(notificationId);
        assertEquals(expectedNotification, resultNotification);
    }

    @Test
    void testGetOne_WhenNotificationDoesNotExist() {
        // Given
        String notificationId = "1";

        // When
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            notificationService.getOne(notificationId);
        });
    }

    @Test
    void testGetAllByEventId_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String eventId = "eventId";
        Notification notification = new Notification("1",
                eventId, null, null, null, null);
        List<Notification> notificationList = List.of(notification);
        Page<Notification> expectedPage = new PageImpl<>(notificationList, pageable, notificationList.size());

        // When
        when(notificationRepository.findByEventId(eventId, pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Notification> result = notificationService.getAllByEventId(eventId, pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
        assertEquals(eventId, result.getCollection().get(0).getEventId());
    }

    @Test
    void testGetAllByEventId_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String eventId = "eventId";
        List<Notification> notificationList = List.of();
        Page<Notification> emptyPage = new PageImpl<>(notificationList, pageable, notificationList.size());

        // When
        when(notificationRepository.findByEventId(eventId, pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Notification> result = notificationService.getAllByEventId(eventId, pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testSave_WithValidDto() throws DataIntegrityException {
        // Given
        User user = new User("1", "username", null);
        NotificationDto validDto = new NotificationDto(
                "EventId",
                user,
                user,
                null,
                null);

        // When
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        Notification savedNotification = notificationService.save(validDto);
        assertNotNull(savedNotification);
        assertEquals(validDto.getEventId(), savedNotification.getEventId());
    }

    @Test
    void testSave_WithEmptyDto() {
        // Given
        NotificationDto emptyDto = new NotificationDto();

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            notificationService.save(emptyDto);
        });
    }
}
