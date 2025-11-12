package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends ControllerException {
    public UnAuthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
