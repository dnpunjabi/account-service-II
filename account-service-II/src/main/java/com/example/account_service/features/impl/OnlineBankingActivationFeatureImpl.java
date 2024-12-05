package com.example.account_service.features.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.account_service.audit.ApiCallLogService;
import com.example.account_service.config.ProductConfig;
import com.example.account_service.features.ProductFeature;
import com.example.account_service.util.RestClientUtilDummy;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OnlineBankingActivationFeatureImpl implements ProductFeature {

    private final RestClientUtilDummy restClientUtil;
    private final ApiCallLogService apiCallLogService;
    private final ProductConfig productConfig;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON serialization

    @Autowired
    public OnlineBankingActivationFeatureImpl(RestClientUtilDummy restClientUtil,
                                              ApiCallLogService apiCallLogService,
                                              ProductConfig productConfig) {
        this.restClientUtil = restClientUtil;
        this.apiCallLogService = apiCallLogService;
        this.productConfig = productConfig;
    }

    @Override
    public void execute(Map<String, Object> requestContext) throws Exception {
        String brand = (String) requestContext.get("brand");
        String productCode = (String) requestContext.get("productCode");
        String transactionId = (String) requestContext.get("transactionId");
        String fkn = (String) requestContext.get("fkn");
        boolean onlineBankingOptIn = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false);

        // Retrieve the product config for the brand and product
        ProductConfig.Product product = productConfig.getBrands().get(brand).getProducts().stream()
            .filter(p -> p.getProductCode().equalsIgnoreCase(productCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found for brand: " + brand));

        // Check if online banking is enabled
        ProductConfig.OnlineBankingFeature onlineBanking = product.getFeatures().getOnlineBankingActivation();
        if (!onlineBanking.isEnabled() || !onlineBankingOptIn) {
            log.info("Online banking activation is not enabled or opted-in for brand: {}, product: {}", brand, productCode);
            return;
        }

        // Prepare sub-feature attributes
        ProductConfig.SubFeatures subFeatures = onlineBanking.getSubFeatures();

        // Build the custom payload for the Online Banking Activation API
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transactionId);
        payload.put("fkn", fkn);
        payload.put("productCode", productCode);
        payload.put("customerType", requestContext.get("customerType")); // Example: Individual or Corporate
        payload.put("channel", "digital-banking"); // Constant field
        payload.put("simulateFailure", Optional.ofNullable(requestContext.get("simulateFailure")).map(String::valueOf).orElse("NONE"));
        payload.put("failureTarget", Optional.ofNullable(requestContext.get("failureTarget")).map(String::valueOf).orElse("NONE"));

        // Add sub-feature attributes to the payload
        payload.put("telephoneBanking", subFeatures.isTelephoneBanking());
        payload.put("smsNotifications", subFeatures.isSmsNotifications());
        payload.put("emailAlerts", subFeatures.isEmailAlerts());

        log.info("Payload for Online Banking Activation: {}", payload);

        // Send the payload in the API call
        String url = "https://upstream.api/activate-online-banking";
        ResponseEntity<String> response = restClientUtil.makePostCall(url, payload);

        // Log the request and response in the database
        String jsonPayload = objectMapper.writeValueAsString(payload);
        apiCallLogService.logApiResponse(
            transactionId,
            "activate-online-banking",
            fkn,
            productCode,
            response.getStatusCode().toString(),
            jsonPayload,
            response.getBody()
        );

        // Handle non-successful responses
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Online Banking Activation failed for productCode: " + productCode +
                ", transactionId: " + transactionId +
                ", fkn: " + fkn +
                " with status: " + response.getStatusCode());
        }

        // Log success
        log.info("Online Banking Activation successfully completed for product: {}, transactionId: {}, fkn: {}",
            productCode, transactionId, fkn);
    }
}