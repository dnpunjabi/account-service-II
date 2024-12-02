package com.example.account_service.util;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestClientUtilDummy {


    public RestClientUtilDummy(RestTemplate restTemplate) {
    }
/**
     * Simulates a POST request to an upstream API.
     * If the request payload contains 'simulateFailure' and 'failureTarget', it simulates a specific failure for a specific API.
     */
    public ResponseEntity<String> makePostCall(String url, Map<String, Object> requestPayload) {
        log.info("Simulating POST request to URL: {} with payload: {}", url, requestPayload);

        // Extract simulateFailure and failureTarget from the payload
        String simulateFailure = (String) requestPayload.getOrDefault("simulateFailure", "NONE");
        String failureTarget = (String) requestPayload.getOrDefault("failureTarget", "NONE");

        // Check if the current URL matches the failure target
        if (failureTarget != null && !failureTarget.equals("NONE") && url.contains(failureTarget)) {
            switch (simulateFailure) {
                case "NETWORK_ERROR":
                    log.error("Simulating network error for API: {}", failureTarget);
                    return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated network error for API " + failureTarget + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
                case "SERVICE_UNAVAILABLE":
                    log.error("Simulating service unavailable error for API: {}", failureTarget);
                    return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Service temporarily unavailable for API " + failureTarget + "\"}", HttpStatus.SERVICE_UNAVAILABLE);
                case "BAD_REQUEST":
                    log.error("Simulating bad request error for API: {}", failureTarget);
                    return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated bad request for API " + failureTarget + "\"}", HttpStatus.BAD_REQUEST);
                default:
                    log.error("Simulating generic API failure for API: {}", failureTarget);
                    return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated API failure for " + failureTarget + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Simulated Success Responses

        if (url.contains("schufa-check")) {
            log.info("Simulating SCHUFA Check API call...");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"SCHUFA Check passed successfully\"}", HttpStatus.OK);
        } else if (url.contains("account-opening")) {
            log.info("Simulating successful account opening response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Onboarding successful\"}", HttpStatus.OK);
        } else if (url.contains("activate-pin")) {
            log.info("Simulating successful PIN activation response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"PIN activated\"}", HttpStatus.OK);
        } else if (url.contains("activate-online-banking")) {
            log.info("Simulating successful online banking activation response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Online banking activated\"}", HttpStatus.OK);
        }

        // Default error response for unknown URLs
        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND);
    }

    /**
     * Simulates a POST request to Orinoco Case Management API.
     *
     * @param url The URL to call.
     * @param casePayload The payload containing case details.
     * @return A ResponseEntity simulating the Orinoco API response (success or failure).
     */
    public ResponseEntity<String> makeCaseManagementCall(String url, Object casePayload) {
        log.info("Simulating Orinoco Case Management POST request to URL: {} with payload: {}", url, casePayload);

        // Simulate success or failure based on casePayload (for testing, we can add a failure flag in the payload)
        if (url.contains("case-management")) {
            // Simulate a successful case creation response
            log.info("Simulating successful Orinoco Case Management response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Case management notified successfully\"}", HttpStatus.OK);
        }

        // Simulate failure for testing purposes
        if (url.contains("error-trigger")) {
            log.error("Simulating Orinoco Case Management API failure");
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated Orinoco Case Management API failure\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Default response for unknown URLs
        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND);
    }

    /**
     * Simulates a GET request to an upstream API.
     */
    public ResponseEntity<String> makeGetCall(String url) {
        log.info("Simulating GET request to URL: {}", url);

        if (url.contains("check-status")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Status checked\"}", HttpStatus.OK);
        }

        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND); // Default for unknown URLs
    }
}