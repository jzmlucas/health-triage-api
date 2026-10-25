package com.health_ai_project.symptomservice.client;

import com.health_ai_project.symptomservice.exception.UserNotFoundInUserServiceException;
import com.health_ai_project.symptomservice.exception.UserServiceCommunicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserClient(
            RestTemplate restTemplate,
            @Value("${user-service.url}") String userServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public void validateUser(Long userId) {
        String url = userServiceUrl + "/users/" + userId;

        try {
            restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, authenticatedRequest(), Object.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundInUserServiceException("Usuario nao encontrado no user-service");
            }
            throw new UserServiceCommunicationException("Falha ao validar usuario no user-service", ex);
        } catch (ResourceAccessException ex) {
            throw new UserServiceCommunicationException("Nao foi possivel conectar ao user-service", ex);
        } catch (RestClientException ex) {
            throw new UserServiceCommunicationException("Erro inesperado ao chamar o user-service", ex);
        }
    }

    private HttpEntity<Void> authenticatedRequest() {
        HttpHeaders headers = new HttpHeaders();
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        return new HttpEntity<>(headers);
    }
}
