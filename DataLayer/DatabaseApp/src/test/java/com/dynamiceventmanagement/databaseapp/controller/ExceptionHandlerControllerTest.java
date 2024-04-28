package com.dynamiceventmanagement.databaseapp.controller;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.response.StandardErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ExceptionHandlerControllerTest {

    @InjectMocks
    private ExceptionHandlerController exceptionHandlerController;

    @BeforeEach
    void setUp() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        new CustomPropertiesBean(mockEnvironment);
    }

    @Test
    void testHandleDataNotFoundException() {
        // Given
        String errorMessage = CustomPropertiesBean.getProperty("exception.data.not-found.generic");
        DataNotFoundException exception = new DataNotFoundException(errorMessage);

        // When

        // Then
        ResponseEntity<?> responseEntity = exceptionHandlerController.handleDataNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorMessage,
                ((StandardErrorResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    void testHandleDataIntegrityException() {
        // Given
        String errorMessage = CustomPropertiesBean.getProperty("exception.data.integrity.generic");
        DataIntegrityException exception = new DataIntegrityException(errorMessage);

        // When

        // Then
        ResponseEntity<?> responseEntity = exceptionHandlerController.handleDataIntegrityException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(errorMessage,
                ((StandardErrorResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    void testHandleDuplicateKeyException() {
        // Given
        String errorMessage = CustomPropertiesBean.getProperty("exception.data.duplicate-Key.generic");
        DuplicateKeyException exception = new DuplicateKeyException(errorMessage);

        // When

        // Then
        ResponseEntity<?> responseEntity = exceptionHandlerController.handleDuplicateKeyException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(errorMessage,
                ((StandardErrorResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    void testHandleException() {
        // Given
        String errorMessage = CustomPropertiesBean.getProperty("exception.generic");
        Exception exception = new Exception(errorMessage);

        // When

        // Then
        ResponseEntity<?> responseEntity = exceptionHandlerController.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(errorMessage,
                ((StandardErrorResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }
}
