package com.example.account_service.util;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestClientUtil {

    private final RestTemplate restTemplate;
    private final SidecarTokenService sidecarTokenService;

    public RestClientUtil(RestTemplate restTemplate, SidecarTokenService sidecarTokenService) {
        this.restTemplate = restTemplate;
        this.sidecarTokenService = sidecarTokenService;
    }

    /**
     * Adds `Authorization` and `X-Authorization` headers using tokens fetched from the `SidecarTokenService`.
     */
    public ResponseEntity<String> makePostCall(String url, Map<String, Object> requestPayload) {
        log.info("Making POST request to URL: {} with payload: {}", url, requestPayload);

        try {
            // Fetch tokens using SidecarTokenService
            String authnToken = sidecarTokenService.fetchAuthnToken(); // JWT R token
            String authzToken = sidecarTokenService.fetchAuthzToken(); // JWT G token

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authnToken); // Authentication token
            headers.set("X-Authorization", "Bearer " + authzToken); // Authorization token
            headers.set("Content-Type", "application/json");

            // Build the request entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            // Make the POST call
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        } catch (RestClientException e) {
            log.error("Error making POST call to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("Error making POST call to " + url, e);
        }
    }

    /**
     * Adds `Authorization` and `X-Authorization` headers using tokens fetched from the `SidecarTokenService`.
     */
    public ResponseEntity<String> makeGetCall(String url) {
        log.info("Making GET request to URL: {}", url);

        try {
            // Fetch tokens using SidecarTokenService
            String authnToken = sidecarTokenService.fetchAuthnToken(); // JWT R token
            String authzToken = sidecarTokenService.fetchAuthzToken(); // JWT G token

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authnToken); // Authentication token
            headers.set("X-Authorization", "Bearer " + authzToken); // Authorization token
            headers.set("Content-Type", "application/json");

            // Build the request entity
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Make the GET call
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        } catch (RestClientException e) {
            log.error("Error making GET call to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("Error making GET call to " + url, e);
        }
    }
}