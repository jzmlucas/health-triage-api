package com.health_ai_project.symptomservice.exception;

public class UserNotFoundInUserServiceException extends RuntimeException {

    public UserNotFoundInUserServiceException(String message) {
        super(message);
    }
}
