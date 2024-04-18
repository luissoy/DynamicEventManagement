package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.EventDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Event;
import com.dynamiceventmanagement.databaselayer.repository.EventRepository;
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
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testGetAll_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Event event = new Event("1", "GroupId", "UserId", null);
        List<Event> eventList = List.of(event);
        Page<Event> expectedPage = new PageImpl<>(eventList, pageable, eventList.size());

        // When
        when(eventRepository.findAll(pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Event> result = eventService.getAll(pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
    }

    @Test
    void testGetAll_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Event> eventList = List.of();
        Page<Event> emptyPage = new PageImpl<>(eventList, pageable, eventList.size());

        // When
        when(eventRepository.findAll(pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Event> result = eventService.getAll(pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testGetOne_WhenEventExists() throws DataNotFoundException {
        // Given
        String eventId = "1";
        Event expectedEvent = new Event(eventId, "GroupId", "UserId", null);

        // When
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(expectedEvent));

        // Then
        Event resultEvent = eventService.getOne(eventId);
        assertEquals(expectedEvent, resultEvent);
    }

    @Test
    void testGetOne_WhenEventDoesNotExist() {
        // Given
        String eventId = "1";

        // When
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Then
        assertThrows(DataNotFoundException.class, () -> {
            eventService.getOne(eventId);
        });
    }

    @Test
    void testGetAllByGroupId_NotEmpty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String groupId = "groupId";
        Event event = new Event("eventId", groupId, "UserId", null);
        List<Event> eventList = List.of(event);
        Page<Event> expectedPage = new PageImpl<>(eventList, pageable, eventList.size());

        // When
        when(eventRepository.findByGroupId(groupId, pageable)).thenReturn(expectedPage);

        // Then
        PageResponse<Event> result = eventService.getAllByGroupId(groupId, pageable);
        assertEquals(expectedPage.getContent(), result.getCollection());
        assertEquals(groupId, result.getCollection().get(0).getGroupId());
    }

    @Test
    void testGetAllByGroupId_Empty() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String groupId = "groupId";
        List<Event> eventList = List.of();
        Page<Event> emptyPage = new PageImpl<>(eventList, pageable, eventList.size());

        // When
        when(eventRepository.findByGroupId(groupId, pageable)).thenReturn(emptyPage);

        // Then
        PageResponse<Event> result = eventService.getAllByGroupId(groupId, pageable);
        assertTrue(result.getCollection().isEmpty());
        assertEquals(0, result.getSize());
    }

    @Test
    void testSave_WithValidDto() throws DataIntegrityException {
        // Given
        EventDto validDto = new EventDto("groupId", "userId", null);

        // When
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        Event savedEvent = eventService.save(validDto);
        assertNotNull(savedEvent);
        assertEquals(validDto.getGroupId(), savedEvent.getGroupId());
    }

    @Test
    void testSave_WithEmptyDto() {
        // Given
        EventDto emptyDto = new EventDto();

        // When

        // Then
        assertThrows(DataIntegrityException.class, () -> {
            eventService.save(emptyDto);
        });
    }
}
