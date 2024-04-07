package com.dynamiceventmanagement.databaselayer.controller;

import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.service.AppService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/apps")
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping
    public ResponseEntity<?> getAll(
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).
                body(appService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(appService.getOne(id));
    }

    @GetMapping("names/{name}")
    public ResponseEntity<?> getByName(
            @PathVariable("name") String name)
            throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).
                body(appService.getByName(name));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody AppDto appDto)
            throws DataIntegrityException {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(appService.save(appDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id,
            @RequestBody AppDto appDto)
            throws DataNotFoundException, DataIntegrityException {
        return ResponseEntity.status(HttpStatus.OK).
                body(appService.update(id, appDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(example = "606d1b91df256d34e0a44a35")
            @PathVariable("id") String id)
            throws DataNotFoundException, DataIntegrityException {
        appService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
