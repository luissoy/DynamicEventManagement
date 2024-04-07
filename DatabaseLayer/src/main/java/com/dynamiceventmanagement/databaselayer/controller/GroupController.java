package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.service.GroupService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping
    public ResponseEntity<?> getAll(
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(groupService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(groupService.getOne(id));
    }

    @GetMapping("apps/{appId}")
    public ResponseEntity<?> getByAppId(
            @PathVariable("appId") String appId,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(groupService.getByAppId(appId, pageable));
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<?> getByUserId(
            @PathVariable("userId") String userId,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(groupService.getByUserId(userId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody GroupDto groupDto)
            throws DataIntegrityException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(groupService.save(groupDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id,
            @RequestBody GroupDto groupDto)
            throws DataNotFoundException, DataIntegrityException {
        return ResponseEntity.status(HttpStatus.OK).
                body(groupService.update(id, groupDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        groupService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
