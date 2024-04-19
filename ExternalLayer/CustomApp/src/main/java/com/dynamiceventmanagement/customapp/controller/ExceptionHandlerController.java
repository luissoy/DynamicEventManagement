package com.dynamiceventmanagement.customapp.controller;

import com.dynamiceventmanagement.customapp.response.StandardErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String message = e.getMessage();
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

}
