package com.health_ai_project.aiservice.catalog;

import tools.jackson.databind.JsonNode;
import com.health_ai_project.aiservice.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Pattern;

@Service
public class HealthCodeService {
    private static final Pattern CID10 = Pattern.compile("^[A-TV-Z][0-9]{2}(\\.?[0-9A-Z]{1,4})?$");
    private static final String SOURCE = "https://www.alicercelabs.com.br/apis/oms";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HealthCodeService(
            RestTemplate restTemplate,
            @Value("${health-catalog.base-url:https://api.alicercelabs.com.br/api/v1/oms/cid10}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public HealthCodeResponse lookup(String rawCode) {
        String input = rawCode.toUpperCase(java.util.Locale.ROOT).trim();
        if (!CID10.matcher(input).matches()) {
            throw new IllegalArgumentException("Código CID-10 inválido");
        }

        String normalizedCode = input.replace(".", "");
        try {
            JsonNode root = restTemplate.getForObject(baseUrl + "/" + normalizedCode, JsonNode.class);
            JsonNode data = root == null ? null : root.path("data");
            if (data == null || data.isMissingNode() || !data.hasNonNull("nome")) {
                throw new AiServiceUnavailableException("A fonte CID-10 não retornou um resultado válido.", null);
            }
            return new HealthCodeResponse(
                    data.path("codigo").asText(normalizedCode),
                    data.path("nome").asText(),
                    "AlicerceLabs; dados atribuídos à tradução OMS/OPAS usada no Brasil",
                    SOURCE
            );
        } catch (RestClientException ex) {
            throw new AiServiceUnavailableException("Não foi possível consultar a fonte CID-10 agora.", ex);
        }
    }
}
