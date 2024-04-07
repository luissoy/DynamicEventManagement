package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import com.dynamiceventmanagement.databaselayer.dto.EventDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Event;
import com.dynamiceventmanagement.databaselayer.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping(path = "/api/v1/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAll(
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(eventService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(eventService.getOne(id));
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<?> getAllByGroupId(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(eventService.getAllByGroupId(id, pageable));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody EventDto eventDto)
            throws DataIntegrityException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(eventService.save(eventDto));
    }
}
