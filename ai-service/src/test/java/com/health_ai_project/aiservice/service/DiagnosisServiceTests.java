package com.health_ai_project.aiservice.service;

import com.health_ai_project.aiservice.catalog.HealthCodeService;
import com.health_ai_project.aiservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.aiservice.exception.AiServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class DiagnosisServiceTests {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test void sendsCautiousPortuguesePromptAndNormalizesStructuredRisk() throws Exception {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        String diagnosis = "{\"condition\":\"Pode haver várias causas; não é possível confirmar por aqui.\",\"risk\":\"baixo\",\"recommendation\":\"Procure avaliação se persistir.\",\"red_flags\":[],\"questions\":[]}";
        String response = mapper.writeValueAsString(Map.of("message", Map.of("content", diagnosis)));
        server.expect(requestTo("http://ollama.test/api/chat"))
                .andExpect(method(POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SAMU 192")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"format\":\"json\"")))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        DiagnosisService service = new DiagnosisService(rest, "http://ollama.test", "test-model", mapper);
        DiagnosisRequestDTO request = new DiagnosisRequestDTO();
        request.setSymptoms(List.of("febre há dois dias"));
        var result = service.analyzeSymptoms(request);

        assertThat(result.getDiagnosis()).contains("\"risk\":\"BAIXO\"");
        assertThat(result.getDisclaimer()).contains("não constitui diagnóstico");
        server.verify();
    }

    @Test void rejectsNonJsonModelOutput() throws Exception {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        String response = mapper.writeValueAsString(Map.of("message", Map.of("content", "Não é possível avaliar.")));
        server.expect(requestTo("http://ollama.test/api/chat"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        DiagnosisService service = new DiagnosisService(rest, "http://ollama.test", "test-model", mapper);
        DiagnosisRequestDTO request = new DiagnosisRequestDTO();
        request.setSymptoms(List.of("dor de cabeça"));

        assertThatThrownBy(() -> service.analyzeSymptoms(request))
                .isInstanceOf(AiServiceUnavailableException.class)
                .hasMessageContaining("formato seguro esperado");
        server.verify();
    }

    @Test void looksUpBrazilianCid10CodeWithoutSendingSymptoms() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        server.expect(requestTo("https://cid.test/v1/cid10/E11"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("{\"success\":true,\"data\":{\"codigo\":\"E11\",\"nome\":\"Diabetes mellitus tipo 2\"}}", MediaType.APPLICATION_JSON));
        HealthCodeService service = new HealthCodeService(rest, "https://cid.test/v1/cid10");

        var result = service.lookup("E11");
        assertThat(result.code()).isEqualTo("E11");
        assertThat(result.description()).isEqualTo("Diabetes mellitus tipo 2");
        assertThat(result.source()).contains("AlicerceLabs");
        server.verify();
    }
}
