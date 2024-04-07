package com.dynamiceventmanagement.databaselayer.exception;

public class DataIntegrityException extends Exception {
    public DataIntegrityException() {
    }

    public DataIntegrityException(String message) {
        super(message);
    }
}
