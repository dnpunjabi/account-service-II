package com.example.account_service.features.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.account_service.audit.ApiCallLogService;
import com.example.account_service.features.ProductFeature;
import com.example.account_service.util.RestClientUtilDummy;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PinActivationFeatureImpl implements ProductFeature {

    private final RestClientUtilDummy restClientUtil;
    private final ApiCallLogService apiCallLogService; // Inject the AuditLogService

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PinActivationFeatureImpl(RestClientUtilDummy restClientUtil, ApiCallLogService apiCallLogService) {
        this.restClientUtil = restClientUtil;
        this.apiCallLogService = apiCallLogService;
    }

    @Override
    public void execute(Map<String, Object> requestContext) throws Exception {
        String transactionId = (String) requestContext.get("transactionId");
        String fkn = (String) requestContext.get("fkn");
        String productCode = (String) requestContext.get("productCode");
        boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
        // Using Optional (preferred for null safety):
        String simulateFailure = Optional.ofNullable(requestContext.get("simulateFailure"))
                                        .map(String::valueOf)  // Convert to String if present
                                        .orElse("NONE");      // Default to "NONE" if null or absent

        String failureTarget = Optional.ofNullable(requestContext.get("failureTarget"))
                                        .map(String::valueOf)
                                        .orElse("NONE");


        String url = "https://upstream.api/activate-pin?fkn=" + fkn + "&productCode=" + productCode;

        // Build the custom payload for the PIN Activation API
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transactionId);
        payload.put("fkn", fkn);
        payload.put("productCode", productCode);
        payload.put("pinSet", pinSet);  // Coming from requestContext
        payload.put("activationChannel", "mobile");  // Constant value
        payload.put("simulateFailure", simulateFailure);  // Derived from request context
        payload.put("failureTarget", failureTarget);  // Derived from request context
        // Add any derived fields if necessary (for example, onboarding calculated values)
               // Derived fields, e.g., activation timestamp
        //payload.put("activationTimestamp", System.currentTimeMillis());

        // Send the payload in the API call
        ResponseEntity<String> response = restClientUtil.makePostCall(url, payload);

        String jsonString = objectMapper.writeValueAsString(payload);
        // Log the response in the database

        apiCallLogService.logApiResponse(
                transactionId,
                "activate-pin",
                fkn,
                productCode,
                response.getStatusCode().toString(),
                jsonString,
                response.getBody()
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("PIN Activation failed for productCode: " + productCode +
            ", transactionId: " + transactionId +
            ", fkn: " + fkn +
            " with status: " + response.getStatusCode());
}

            // Log success
            log.info("PIN Activation completed successfully for product: {}, transactionId: {}, fkn: {}",
            productCode, transactionId, fkn);
            }
}