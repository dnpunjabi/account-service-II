package com.example.account_service.features.impl;

import java.util.HashMap;
import java.util.Map;

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
        // Retrieve the product configuration
        ProductConfig.Product product = productConfig.getBrands().get(brand).getProducts().stream()
            .filter(p -> p.getProductCode().equalsIgnoreCase(productCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found for brand: " + brand));

        // Check if online banking is enabled
        ProductConfig.OnlineBankingFeature onlineBanking = product.getFeatures().getOnlineBankingActivation();
        

        // Retrieve subfeatures
        Map<String, Boolean> subFeatures = getSubFeatures(onlineBanking);

        // Build the custom payload for the Online Banking Activation API
        Map<String, Object> payload = buildPayload(requestContext, productCode, subFeatures);

        log.info("Payload for Online Banking Activation: {}", payload);

        // Send the payload in the API call
        String url = "https://upstream.api/activate-online-banking";
        ResponseEntity<String> response = restClientUtil.makePostCall(url, payload);

        // Log the request and response in the database
        logApiCall(requestContext, productCode, payload, response);

        // Handle non-successful responses
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Online Banking Activation failed for productCode: " + productCode +
                " with status: " + response.getStatusCode());
        }

        log.info("Online Banking Activation successfully completed for product: {}", productCode);
    }

    private Map<String, Boolean> getSubFeatures(ProductConfig.OnlineBankingFeature onlineBanking) {
        Map<String, Boolean> subFeaturesMap = new HashMap<>();
        ProductConfig.SubFeatures subFeatures = onlineBanking.getSubFeatures();

        if (subFeatures != null) {
            subFeaturesMap.put("telephoneBanking", subFeatures.isTelephoneBanking());
            subFeaturesMap.put("smsNotifications", subFeatures.isSmsNotifications());
            subFeaturesMap.put("emailAlerts", subFeatures.isEmailAlerts());
        }

        return subFeaturesMap;
    }

    private Map<String, Object> buildPayload(Map<String, Object> requestContext, String productCode, Map<String, Boolean> subFeatures) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", requestContext.get("transactionId"));
        payload.put("fkn", requestContext.get("fkn"));
        payload.put("productCode", productCode);
        payload.put("customerType", requestContext.get("customerType"));
        payload.put("channel", "digital-banking");
        payload.put("simulateFailure", requestContext.get("simulateFailure"));
        payload.put("failureTarget", requestContext.get("failureTarget"));
        payload.putAll(subFeatures); // Add subfeatures to the payload
        return payload;
    }

    private void logApiCall(Map<String, Object> requestContext, String productCode, Map<String, Object> payload, ResponseEntity<String> response) throws Exception {
        String transactionId = (String) requestContext.get("transactionId");
        String jsonPayload = objectMapper.writeValueAsString(payload);

        apiCallLogService.logApiResponse(
            transactionId,
            "activate-online-banking",
            (String) requestContext.get("fkn"),
            productCode,
            response.getStatusCode().toString(),
            jsonPayload,
            response.getBody()
        );
    }
}