package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends ControllerException{
    public UsernameAlreadyExistsException(String message) {
        super(message, HttpStatus.ALREADY_REPORTED);
    }
}
