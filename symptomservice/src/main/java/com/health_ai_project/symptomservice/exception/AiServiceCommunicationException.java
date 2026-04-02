package com.health_ai_project.symptomservice.exception;

public class AiServiceCommunicationException extends RuntimeException {

    public AiServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
