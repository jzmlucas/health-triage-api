package com.health_ai_project.aiservice.dto;

public class DiagnosisResponseDTO {

    private String diagnosis;
    private String recommendation;
    private String disclaimer;
    private String model;

    public DiagnosisResponseDTO() {
    }

    public DiagnosisResponseDTO(String diagnosis, String recommendation, String disclaimer, String model) {
        this.diagnosis = diagnosis;
        this.recommendation = recommendation;
        this.disclaimer = disclaimer;
        this.model = model;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
