package com.health_ai_project.aiservice.service;

import com.health_ai_project.aiservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.aiservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.aiservice.dto.OllamaChatRequest;
import com.health_ai_project.aiservice.dto.OllamaChatResponse;
import com.health_ai_project.aiservice.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DiagnosisService {

    private static final String DISCLAIMER = "This AI output is for testing and educational use only. It is not a medical diagnosis and does not replace a doctor.";

    private final RestTemplate restTemplate;
    private final String ollamaBaseUrl;
    private final String ollamaModel;

    public DiagnosisService(
            RestTemplate restTemplate,
            @Value("${ollama.base-url}") String ollamaBaseUrl,
            @Value("${ollama.model}") String ollamaModel
    ) {
        this.restTemplate = restTemplate;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaModel = ollamaModel;
    }

    public DiagnosisResponseDTO analyzeSymptoms(DiagnosisRequestDTO request) {
        String prompt = buildPrompt(request.getSymptoms());

        OllamaChatRequest ollamaRequest = new OllamaChatRequest(
                ollamaModel,
                List.of(
                        new OllamaChatRequest.Message(
                                "system",
                                """
                                Você é um assistente de triagem médica que irá ajudar o usuario em seu diagnóstico.

                                Regras:
                                - Responda sempre em português do Brasil
                                - Não forneça diagnóstico definitivo
                                - Seja direto, claro e objetivo

                                Retorne APENAS um JSON válido no formato:

                                {
                                "condition": "possível condição",
                                "risk": "BAIXO | MEDIO | ALTO",
                                "recommendation": "orientação clara ao usuário"
                                }
                                """
                        ),
                        new OllamaChatRequest.Message(
                                "user",
                                prompt
                        )
                ),
                false
        );

        try {
            OllamaChatResponse response = restTemplate.postForObject(
                    ollamaBaseUrl + "/api/chat",
                    ollamaRequest,
                    OllamaChatResponse.class
            );

            String content = extractContent(response);
            return new DiagnosisResponseDTO(
                    content,
                    "If symptoms are severe, persistent, or worsening, seek medical evaluation.",
                    DISCLAIMER,
                    ollamaModel
            );
        } catch (ResourceAccessException ex) {
            throw new AiServiceUnavailableException("Could not connect to Ollama. Start Ollama and ensure a model is installed.", ex);
        } catch (RestClientException ex) {
            throw new AiServiceUnavailableException("Failed to generate AI analysis from Ollama.", ex);
        }
    }

    private String buildPrompt(List<String> symptoms) {
        return "Analyze these symptoms and provide a short, cautious health assessment in Portuguese from Brazil: " + String.join(", ", symptoms);
    }

    private String extractContent(OllamaChatResponse response) {
        if (response == null || response.getMessage() == null || response.getMessage().getContent() == null) {
            throw new AiServiceUnavailableException("Ollama returned an empty response.", null);
        }
        return response.getMessage().getContent();
    }
}
