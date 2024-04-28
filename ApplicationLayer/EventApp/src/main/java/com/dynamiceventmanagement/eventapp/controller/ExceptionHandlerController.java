package com.dynamiceventmanagement.eventapp.controller;

import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.response.StandardErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(DatabaseApiException.class)
    public ResponseEntity<?> handleDatabaseApiException(DatabaseApiException e) {
        String message = e.getMessage();
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<?> handleHttpClientErrorException_NotFound(HttpClientErrorException.NotFound e) {
        String message = e.getMessage();
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String message = e.getMessage();
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

}
