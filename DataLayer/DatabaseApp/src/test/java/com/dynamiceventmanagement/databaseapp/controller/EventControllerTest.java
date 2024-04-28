package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.dto.EventDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Event;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.service.EventService;
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
public class EventControllerTest {
    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @Test
    void testGetAll() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Event event = new Event("1", "GroupId", "UserId", null);
        List<Event> eventList = List.of(event);
        Page<Event> page = new PageImpl<>(eventList, pageable, eventList.size());

        PageResponse<Event> pageResponse = new PageResponse<>(page);

        // When
        when(eventService.getAll(pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = eventController.getAll(pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
    }

    @Test
    void testGetOne() throws DataNotFoundException {
        // Given
        String id = "1";
        Event event = new Event(id, "GroupId", "UserId", null);

        // When
        when(eventService.getOne(id)).thenReturn(event);

        // Then
        ResponseEntity<?> responseEntity = eventController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(event, responseEntity.getBody());
    }

    @Test
    void testGetAllByEventId() {
        // Given
        Pageable pageable = Pageable.unpaged();
        String groupId = "groupId";
        Event event = new Event("1", groupId, "UserId", null);
        List<Event> eventList = List.of(event);
        Page<Event> page = new PageImpl<>(eventList, pageable, eventList.size());

        PageResponse<Event> pageResponse = new PageResponse<>(page);

        // When
        when(eventService.getAllByGroupId(groupId, pageable)).thenReturn(pageResponse);

        // Then
        ResponseEntity<?> responseEntity = eventController.getAllByGroupId(groupId, pageable);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pageResponse, responseEntity.getBody());
        assertEquals(groupId,
                ((PageResponse<Event>) responseEntity.getBody()).getCollection().get(0).getGroupId());
    }

    @Test
    void testSave() throws DataIntegrityException {
        // Given
        EventDto eventDto = new EventDto("groupId", "UserId", null);
        Event event = new Event("1", eventDto);

        // When
        when(eventService.save(eventDto)).thenReturn(event);

        // Then
        ResponseEntity<?> responseEntity = eventController.save(eventDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(event, responseEntity.getBody());
    }
}