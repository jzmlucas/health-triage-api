package com.health_ai_project.symptomservice.exception;

public class UserServiceCommunicationException extends RuntimeException {

    public UserServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
