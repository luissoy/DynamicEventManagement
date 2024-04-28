package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.response.StandardErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<?> handleDataNotFoundException(DataNotFoundException e) {
        String message = e.getMessage() == null ?
                CustomPropertiesBean.getProperty("exception.data.not-found.generic") :
                e.getMessage();
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<?> handleDataIntegrityException(DataIntegrityException e) {
        String message = e.getMessage() == null ?
                CustomPropertiesBean.getProperty("exception.data.integrity.generic") :
                e.getMessage();
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e) {
        String message = e.getMessage() == null ?
                CustomPropertiesBean.getProperty("exception.data.duplicate-Key.generic") :
                e.getMessage();
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String message = e.getMessage() == null ?
                CustomPropertiesBean.getProperty("exception.generic") :
                e.getMessage();
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(new StandardErrorResponse(message, e.getClass().getName()));
    }

}
