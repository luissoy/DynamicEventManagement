package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.NotificationDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.service.NotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getAll(
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(notificationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(notificationService.getOne(id));
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<?> getAllByEventId(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(notificationService.getAllByEventId(id, pageable));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody NotificationDto notificationDto)
            throws DataIntegrityException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(notificationService.save(notificationDto));
    }
}
