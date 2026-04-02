package com.health_ai_project.symptomservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SymptomRequestDTO {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotEmpty(message = "Symptoms list cannot be empty")
    private List<String> symptoms;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}
