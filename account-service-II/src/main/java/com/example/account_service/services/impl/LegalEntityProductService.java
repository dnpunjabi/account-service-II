package com.example.account_service.services.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.account_service.config.ProductConfig;
import com.example.account_service.features.factory.ProductFeatureFactory;
import com.example.account_service.services.OrinocoCaseManagementService;
import com.example.account_service.services.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LegalEntityProductService implements ProductService {

    private final ProductFeatureFactory featureFactory;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;

    @Autowired
    public LegalEntityProductService(ProductFeatureFactory featureFactory,
                                     ProductConfig productConfig,
                                     OrinocoCaseManagementService caseManagementService) {
        this.featureFactory = featureFactory;
        this.productConfig = productConfig;
        this.caseManagementService = caseManagementService;
    }

    @Override
    public String process(Map<String, Object> requestContext) {
        String brand = (String) requestContext.get("brand");
        String productCode = (String) requestContext.get("productCode");

        // Retrieve product configuration for the given brand and product
        ProductConfig.Product product = productConfig.getBrands().get(brand).getProducts().stream()
            .filter(p -> p.getProductCode().equalsIgnoreCase(productCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found for brand: " + brand));

        try {
            log.info("Processing Legal Entity Onboarding for brand: {}, product: {}", brand, productCode);

            // Skip SCHUFA Check for legal entities
            log.info("Skipping SCHUFA Check for legal entities");

            // Account Opening
            if (product.getFeatures().isAccountOpening()) {
                log.info("Executing Account Opening for brand: {}, product: {}", brand, productCode);
                featureFactory.getFeature("account-opening").execute(requestContext);
            }

            // PIN Activation
            boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
            if (product.getFeatures().isPinActivation() && pinSet) {
                log.info("Executing PIN Activation for brand: {}, product: {}", brand, productCode);
                featureFactory.getFeature("activate-pin").execute(requestContext);
            } else if (product.getFeatures().isPinActivation() && !pinSet) {
                log.info("Skipping PIN Activation for brand: {}, product: {} (PIN not set)", brand, productCode);
            }

               // Online Banking Activation
               boolean onlineBankingRequested = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false); // Assuming a flag in the request context
               if (product.getFeatures().getOnlineBankingActivation().isEnabled() && onlineBankingRequested) {
                   featureFactory.getFeature("activate-online-banking").execute(requestContext);
               }else if (product.getFeatures().getOnlineBankingActivation().isEnabled() && !onlineBankingRequested) {
                   log.info("Skipping online banking activation as the customer did not request it.");
               }

        } catch (Exception ex) {
            log.error("Error during onboarding for brand: {}, product: {}. Error: {}", brand, productCode, ex.getMessage());
            return handleFailure(requestContext, ex.getMessage());
        }

        log.info("Successfully completed onboarding for Legal Entity Product: {}", productCode);
        return null;
    }

    /**
     * Handles failures by notifying the Orinoco Case Management system.
     */
    private String handleFailure(Map<String, Object> requestContext, String errorDetails) {
        log.error("Handling failure for request: {}. Error: {}", requestContext, errorDetails);
        return caseManagementService.notifyBackOffice(requestContext, errorDetails);
    }
}