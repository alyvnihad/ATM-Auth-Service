package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ControllerException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
