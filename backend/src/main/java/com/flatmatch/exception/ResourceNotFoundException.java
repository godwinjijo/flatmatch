package com.flatmatch.exception;

/** Thrown when something is requested by id but doesn't exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
