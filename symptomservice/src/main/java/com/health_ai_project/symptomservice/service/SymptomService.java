package com.health_ai_project.symptomservice.service;

import com.health_ai_project.symptomservice.client.AiClient;
import com.health_ai_project.symptomservice.client.UserClient;
import com.health_ai_project.symptomservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.symptomservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.symptomservice.dto.SymptomRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class SymptomService {

    private final UserClient userClient;
    private final AiClient aiClient;

    public SymptomService(UserClient userClient, AiClient aiClient) {
        this.userClient = userClient;
        this.aiClient = aiClient;
    }

    public DiagnosisResponseDTO processSymptoms(SymptomRequestDTO dto) {
        userClient.validateUser(dto.getUserId());
        return aiClient.generateDiagnosis(new DiagnosisRequestDTO(dto.getSymptoms()));
    }
}
