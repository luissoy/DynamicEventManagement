package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.EventDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Event;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.service.EventService;
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
        Event event = new Event("1", "GroupId", "info", "UserId");
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
        Event event = new Event(id, "GroupId", "info", "UserId");

        // When
        when(eventService.getOne(id)).thenReturn(event);

        // Then
        ResponseEntity<?> responseEntity = eventController.getOne(id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(event, responseEntity.getBody());
    }

    @Test
    void testGetAllByEventId() throws DataNotFoundException {
        // Given
        Pageable pageable = Pageable.unpaged();
        String groupId = "groupId";
        Event event = new Event("1", groupId, "info", "UserId");
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
        EventDto eventDto = new EventDto("groupId","GroupInfo", "UserId");
        Event event = new Event("1", eventDto);

        // When
        when(eventService.save(eventDto)).thenReturn(event);

        // Then
        ResponseEntity<?> responseEntity = eventController.save(eventDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(event, responseEntity.getBody());
    }
}