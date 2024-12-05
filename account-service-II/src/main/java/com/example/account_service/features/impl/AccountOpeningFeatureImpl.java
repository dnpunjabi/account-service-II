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
public class AccountOpeningFeatureImpl implements ProductFeature {

    private final RestClientUtilDummy restClientUtil;
    private final ApiCallLogService apiCallLogService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AccountOpeningFeatureImpl(RestClientUtilDummy restClientUtil, ApiCallLogService apiCallLogService) {
        this.restClientUtil = restClientUtil;
        this.apiCallLogService = apiCallLogService;
    }

    @Override
    public void execute(Map<String, Object> requestContext) throws Exception {
        String transactionId = (String) requestContext.get("transactionId");
        String fkn = (String) requestContext.get("fkn");
        String productCode = (String) requestContext.get("productCode");
        // Using Optional (preferred for null safety):
        String simulateFailure = Optional.ofNullable(requestContext.get("simulateFailure"))
                                        .map(String::valueOf)  // Convert to String if present
                                        .orElse("NONE");      // Default to "NONE" if null or absent

        String failureTarget = Optional.ofNullable(requestContext.get("failureTarget"))
                                        .map(String::valueOf)
                                        .orElse("NONE");

        String url = "https://upstream.api/account-opening?fkn=" + fkn + "&productCode=" + productCode;

        // Build the custom payload for the Onboarding API
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transactionId);
        payload.put("fkn", fkn);
        payload.put("productCode", productCode);
        payload.put("customerType", requestContext.get("customerType"));  // Example of adding customer type
        payload.put("channel", "digital-banking");  // Constant field
        payload.put("simulateFailure", simulateFailure);  // Derived from request context
        payload.put("failureTarget", failureTarget);  // Derived from request context
        // Add any derived fields if necessary (for example, onboarding calculated values)
        //payload.put("timestamp", System.currentTimeMillis());  // Example of adding a derived field

        // Send the payload in the API call
        ResponseEntity<String> response = restClientUtil.makePostCall(url, payload);

        String jsonString = objectMapper.writeValueAsString(payload);
        // Log the response in the database
        apiCallLogService.logApiResponse(
            transactionId,
            "account-opening",
            fkn,
            productCode,
            response.getStatusCode().toString(),
            jsonString,
            response.getBody()
        );


        // Handle non-successful responses
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Account Opening failed for productCode: " + productCode +
                              ", transactionId: " + transactionId +
                              ", fkn: " + fkn +
                              " with status: " + response.getStatusCode());
       }

       // Log success
       log.info("Account Opening completed successfully for product: {}, transactionId: {}, fkn: {}",
                productCode, transactionId, fkn);
   }
}
