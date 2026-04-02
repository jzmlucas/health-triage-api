package com.health_ai_project.aiservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class DiagnosisRequestDTO {

    @NotEmpty(message = "Symptoms list cannot be empty")
    private List<String> symptoms;

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}
