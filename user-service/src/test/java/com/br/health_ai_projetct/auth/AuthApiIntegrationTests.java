package com.br.health_ai_projetct.auth;

import com.br.health_ai_projetct.repository.UserRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiIntegrationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired JsonMapper mapper;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach void cleanUsers() { users.deleteAll(); }

    @Test void registrationIssuesJwtAndProtectsAccount() throws Exception {
        var registration = post("/auth/register", Map.of("name", "Ana", "email", "ana@example.test", "password", "UmaSenhaForte!2026"), null);
        assertThat(registration.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> body = json(registration);
        assertThat(body).containsKeys("accessToken", "userId", "email");
        long id = ((Number) body.get("userId")).longValue();
        String token = body.get("accessToken").toString();

        assertThat(get("/users/" + id, null).statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        var ownProfile = get("/users/" + id, token);
        assertThat(ownProfile.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(json(ownProfile)).doesNotContainKey("passwordHash");
        assertThat(get("/users/" + (id + 1), token).statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test void loginAndAuthenticatedPasswordChangeWork() throws Exception {
        post("/auth/register", Map.of("name", "Bia", "email", "bia@example.test", "password", "UmaSenhaForte!2026"), null);
        var login = post("/auth/login", Map.of("email", "bia@example.test", "password", "UmaSenhaForte!2026"), null);
        assertThat(login.statusCode()).isEqualTo(HttpStatus.OK.value());
        String token = json(login).get("accessToken").toString();

        var changed = post("/auth/change-password", Map.of("currentPassword", "UmaSenhaForte!2026", "newPassword", "OutraSenhaSegura!2026"), token);
        assertThat(changed.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(post("/auth/login", Map.of("email", "bia@example.test", "password", "UmaSenhaForte!2026"), null).statusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(post("/auth/login", Map.of("email", "bia@example.test", "password", "OutraSenhaSegura!2026"), null).statusCode())
                .isEqualTo(HttpStatus.OK.value());
    }

    @Test void registrationRejectsDuplicateEmailsAndRecoveryDoesNotRevealAccounts() throws Exception {
        var user = Map.of("name", "Caio", "email", "caio@example.test", "password", "UmaSenhaForte!2026");
        assertThat(post("/auth/register", user, null).statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(post("/auth/register", user, null).statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
        var known = post("/auth/forgot-password", Map.of("email", "caio@example.test"), null);
        var unknown = post("/auth/forgot-password", Map.of("email", "unknown@example.test"), null);
        assertThat(known.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(unknown.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(json(known)).isEqualTo(json(unknown));
    }

    private HttpResponse<String> post(String path, Object body, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        if (token != null) request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path)).GET();
        if (token != null) request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }

    private Map<String, Object> json(HttpResponse<String> response) throws Exception {
        return mapper.readValue(response.body(), new TypeReference<>() {});
    }
}
