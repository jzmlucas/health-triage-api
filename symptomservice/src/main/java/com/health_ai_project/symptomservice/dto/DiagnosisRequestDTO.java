package com.health_ai_project.symptomservice.dto;

import java.util.List;

public class DiagnosisRequestDTO {

    private List<String> symptoms;

    public DiagnosisRequestDTO() {
    }

    public DiagnosisRequestDTO(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}
