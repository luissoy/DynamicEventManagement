package com.dynamiceventmanagement.eventapp.controller;

import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path = "/api/v1/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping("/{userId}/{groupId}/{appId}")
    public ResponseEntity<?> save(
            @Parameter(example = "606d1b91df256d34e0a44a35") @PathVariable("userId")
            String userId,
            @Parameter(example = "606d1b91df256d34e0a44a35") @PathVariable("groupId")
            String groupId,
            @Parameter(example = "606d1b91df256d34e0a44a35") @PathVariable("appId")
            String appId,
            @RequestBody Object message)
            throws DatabaseApiException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(eventService.save(userId, groupId, appId, message));
    }
}
