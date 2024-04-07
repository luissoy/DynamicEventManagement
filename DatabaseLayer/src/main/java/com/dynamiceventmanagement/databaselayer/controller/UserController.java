package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.UserDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAll(
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(userService.getOne(id));
    }

    @GetMapping("usernames/{username}")
    public ResponseEntity<?> getByUsername(
            @PathVariable("username") String username)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(userService.getByUsername(username));
    }

    @GetMapping("apps/{appId}")
    public ResponseEntity<?> getByAppId(
            @PathVariable("appId") String appId,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(userService.getByAppId(appId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody UserDto userDto)
            throws DataIntegrityException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(userService.save(userDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id,
            @RequestBody UserDto userDto)
            throws DataNotFoundException, DataIntegrityException {
        return ResponseEntity.status(HttpStatus.OK).
                body(userService.update(id, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException, DataIntegrityException {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
