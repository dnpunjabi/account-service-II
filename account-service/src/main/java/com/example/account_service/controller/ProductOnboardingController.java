package com.example.account_service.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account_service.audit.ApiCallLogService;
import com.example.account_service.config.ProductConfig;
import com.example.account_service.dto.OnboardingRequest;
import com.example.account_service.enums.CustomerType;
import com.example.account_service.services.ProductService;
import com.example.account_service.services.factory.ProductServiceFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductOnboardingController {

    private final ProductServiceFactory productServiceFactory;
    private final ProductConfig productConfig;
    private final ApiCallLogService apiCallLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ProductOnboardingController(ProductServiceFactory productServiceFactory,
                                       ProductConfig productConfig,
                                       ApiCallLogService apiCallLogService) {
        this.productServiceFactory = productServiceFactory;
        this.productConfig = productConfig;
        this.apiCallLogService = apiCallLogService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<String> onboardProduct(@RequestBody OnboardingRequest request) {
        log.info("Received onboarding request: {}", request);

        String transactionId = request.getTransactionId();
        String fkn = request.getFkn();
        String productRequest = request.getProductCode(); // Format: BrandA-BCA

        // Parse the brand and product from the request
        String[] productSplit = productRequest.split("-");
        if (productSplit.length != 2) {
            return ResponseEntity.badRequest().body("Invalid product format. Expected format: BrandName-ProductCode");
        }

        String brandName = productSplit[0];
        String productCode = productSplit[1];

        // Validate the brand and product
        if (!productConfig.getBrands().containsKey(brandName)) {
            return ResponseEntity.badRequest().body("Unsupported brand: " + brandName);
        }
        ProductConfig.BrandConfig brandConfig = productConfig.getBrands().get(brandName);
        if (brandConfig.getProducts().stream().noneMatch(p -> p.getProductCode().equalsIgnoreCase(productCode))) {
            return ResponseEntity.badRequest().body("Unsupported product for brand: " + productCode);
        }

        // Determine customer type
        CustomerType customerType = CustomerType.fromCode(request.getCustomerType());

        // Build request context
        Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("transactionId", transactionId);
        requestContext.put("fkn", fkn);
        requestContext.put("productCode", productCode);
        requestContext.put("simulateFailure", request.getSimulateFailure());
        requestContext.put("failureTarget", request.getFailureTarget());
        requestContext.put("pinSet", request.isPinSet());
        requestContext.put("onlineBankingOptIn", request.isOnlineBankingOptIn());
        requestContext.put("brand", brandName);
        requestContext.put("customerType", customerType);

        log.info("Request Context: {}", requestContext);

        String requestPayloadJson;
        String finalResponseMessage;

        try {
            requestPayloadJson = objectMapper.writeValueAsString(request);

            // Delegate to the appropriate service
            ProductService productService = productServiceFactory.getService(customerType);
            String caseId = productService.process(requestContext);

            // Handle response
            if (caseId != null) {
                finalResponseMessage = "An issue occurred. Case ID: " + caseId +
                                       ". The bank will contact you for further processing.";
            } else {
                finalResponseMessage = "Onboarding for product " + productRequest +
                                       " Transaction Id: " + transactionId +
                                       " linked to FKN " + fkn +
                                       " completed successfully.";
            }

            // Log the final response
            apiCallLogService.logApiResponse(
                transactionId,
                "onboard-product",
                fkn,
                productCode,
                "200 OK",
                requestPayloadJson,
                objectMapper.writeValueAsString(finalResponseMessage)
            );

            return ResponseEntity.ok(finalResponseMessage);

        } catch (Exception e) {
            finalResponseMessage = "Onboarding failed for product " + productRequest +
                                   " Transaction Id: " + transactionId +
                                   " with error: " + e.getMessage();

            log.error("Error during onboarding: {}", e.getMessage(), e);

            try {
                apiCallLogService.logApiResponse(
                    transactionId,
                    "onboard-product",
                    fkn,
                    productCode,
                    "500 INTERNAL_SERVER_ERROR",
                    objectMapper.writeValueAsString(request),
                    objectMapper.writeValueAsString(finalResponseMessage)
                );
            } catch (JsonProcessingException logException) {
                log.error("Error logging to database: {}", logException.getMessage(), logException);
            }

            return ResponseEntity.status(500).body(finalResponseMessage);
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, List<ProductConfig.Product>>> getProductsConfig() {
        // Check if brands is null
        if (productConfig.getBrands() == null) {
            log.error("No brands configured in product-config.yml");
            return ResponseEntity.noContent().build();
        }

        // Retrieve all brand configurations
        Map<String, ProductConfig.BrandConfig> brands = productConfig.getBrands();
        log.info("Retrieving Products Config for all brands. Total Brands: {}", brands.size());

        // Create a response map to include products by each brand
        Map<String, List<ProductConfig.Product>> response = new HashMap<>();
        for (Map.Entry<String, ProductConfig.BrandConfig> entry : brands.entrySet()) {
            String brandName = entry.getKey();
            List<ProductConfig.Product> products = entry.getValue().getProducts();
            response.put(brandName, products);
        }

        return ResponseEntity.ok(response);
    }
}