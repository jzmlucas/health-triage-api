package com.health_ai_project.symptomservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SymptomRequestDTO {

    @NotNull(message = "User id is required")
    @jakarta.validation.constraints.Positive
    private Long userId;

    @NotEmpty(message = "Symptoms list cannot be empty")
    @Size(max = 20, message = "No máximo 20 sintomas por solicitação")
    private List<@jakarta.validation.constraints.NotBlank @Size(max = 200) String> symptoms;

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
