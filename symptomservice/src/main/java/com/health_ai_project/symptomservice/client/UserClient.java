package com.health_ai_project.symptomservice.client;

import com.health_ai_project.symptomservice.exception.UserNotFoundInUserServiceException;
import com.health_ai_project.symptomservice.exception.UserServiceCommunicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
            restTemplate.getForObject(url, Object.class);
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
}
