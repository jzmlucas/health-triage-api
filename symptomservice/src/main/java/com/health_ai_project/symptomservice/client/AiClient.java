package com.health_ai_project.symptomservice.client;

import com.health_ai_project.symptomservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.symptomservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.symptomservice.exception.AiServiceCommunicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AiClient {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiClient(
            RestTemplate restTemplate,
            @Value("${ai-service.url}") String aiServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public DiagnosisResponseDTO generateDiagnosis(DiagnosisRequestDTO request) {
        try {
            return restTemplate.postForObject(
                    aiServiceUrl + "/ai/diagnose",
                    request,
                    DiagnosisResponseDTO.class
            );
        } catch (ResourceAccessException ex) {
            throw new AiServiceCommunicationException("Nao foi possivel conectar ao ai-service", ex);
        } catch (RestClientException ex) {
            throw new AiServiceCommunicationException("Falha ao gerar diagnostico com o ai-service", ex);
        }
    }
}
