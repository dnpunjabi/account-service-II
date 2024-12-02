package com.example.account_service.util;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SidecarTokenService {

    private final RestTemplate restTemplate;

    public SidecarTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the JWT R (authentication) token from the sidecar container.
     * This token is used for authentication in upstream API calls.
     */
    public String fetchAuthnToken() {
        String url = "http://localhost:8082/sidecar/authn-token"; // Replace with actual sidecar endpoint
        log.info("Fetching Authentication Token from: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Fetched Authentication Token successfully.");
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch authentication token. Status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error fetching authentication token: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching authentication token", e);
        }
    }

    /**
     * Fetches the JWT G (authorization) token from the sidecar container.
     * This token is used for authorization in upstream API calls.
     */
    public String fetchAuthzToken() {
        String url = "http://localhost:8082/sidecar/authz-token"; // Replace with actual sidecar endpoint
        log.info("Fetching Authorization Token from: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Fetched Authorization Token successfully.");
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch authorization token. Status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error fetching authorization token: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching authorization token", e);
        }
    }
}