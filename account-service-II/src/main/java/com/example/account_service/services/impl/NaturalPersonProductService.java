package com.example.account_service.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.account_service.config.ProductConfig;
import com.example.account_service.features.builder.FeatureOrderBuilder;
import com.example.account_service.features.factory.ProductFeatureFactory;
import com.example.account_service.services.OrinocoCaseManagementService;
import com.example.account_service.services.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NaturalPersonProductService implements ProductService {

    private final ProductFeatureFactory featureFactory;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;
    private final FeatureOrderBuilder featureOrderBuilder;

    @Autowired
    public NaturalPersonProductService(ProductFeatureFactory featureFactory,
                                       ProductConfig productConfig,
                                       OrinocoCaseManagementService caseManagementService,
                                       FeatureOrderBuilder featureOrderBuilder) {
        this.featureFactory = featureFactory;
        this.productConfig = productConfig;
        this.caseManagementService = caseManagementService;
        this.featureOrderBuilder = featureOrderBuilder;
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
            // Use FeatureOrderBuilder to determine the ordered features
            List<String> executionOrder = featureOrderBuilder.buildFeatureOrder(product);

            // Process each feature in the execution order
            for (String feature : executionOrder) {
                switch (feature) {
                    case "Schufa":
                        featureFactory.getFeature("schufa-check").execute(requestContext);
                        break;
                    case "AccountOpening":
                        featureFactory.getFeature("account-opening").execute(requestContext);
                        break;
                    case "PINActivation":
                        boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
                        if (pinSet) {
                            featureFactory.getFeature("activate-pin").execute(requestContext);
                        } else {
                            log.info("Skipping PIN activation as the customer did not set a PIN.");
                        }
                        break;
                    case "OnlineBankingActivation":
                        boolean onlineBankingRequested = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false);
                        if (onlineBankingRequested) {
                            featureFactory.getFeature("activate-online-banking").execute(requestContext);
                        } else {
                            log.info("Skipping online banking activation as the customer did not request it.");
                        }
                        break;
                    default:
                        log.warn("Unknown feature: {}", feature);
                        break;
                }
            }
        } catch (Exception ex) {
            log.error("Error during processing for brand: {}, product: {}. Error: {}", brand, productCode, ex.getMessage());
            return handleFailure(requestContext, ex.getMessage());
        }

        return null;
    }

    private String handleFailure(Map<String, Object> requestContext, String errorDetails) {
        return caseManagementService.notifyBackOffice(requestContext, errorDetails);
    }
}