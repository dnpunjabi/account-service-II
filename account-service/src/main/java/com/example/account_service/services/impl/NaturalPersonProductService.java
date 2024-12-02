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
public class NaturalPersonProductService implements ProductService {

    private final ProductFeatureFactory featureFactory;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;

    @Autowired
    public NaturalPersonProductService(ProductFeatureFactory featureFactory,
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

        ProductConfig.Product product = productConfig.getBrands().get(brand).getProducts().stream()
            .filter(p -> p.getProductCode().equalsIgnoreCase(productCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found for brand: " + brand));

        try {
            // SCHUFA Check
            if (product.getFeatures().isSchufa()) {
                featureFactory.getFeature("schufa-check").execute(requestContext);
            }

            // Account Opening
            if (product.getFeatures().isAccountOpening()) {
                featureFactory.getFeature("account-opening").execute(requestContext);
            }
            
            boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
         
            // PIN Activation
            if (product.getFeatures().isPinActivation() && pinSet ){
                featureFactory.getFeature("activate-pin").execute(requestContext);
            }else if (product.getFeatures().isPinActivation() && !pinSet) {
                log.info("Skipping PIN activation as the customer did not set a PIN.");
            }

            // Online Banking Activation
            boolean onlineBankingRequested = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false); // Assuming a flag in the request context
            if (product.getFeatures().getOnlineBankingActivation().isEnabled() && onlineBankingRequested) {
                featureFactory.getFeature("activate-online-banking").execute(requestContext);
            }else if (product.getFeatures().getOnlineBankingActivation().isEnabled() && !onlineBankingRequested) {
                log.info("Skipping online banking activation as the customer did not request it.");
            }

        } catch (Exception ex) {
            return handleFailure(requestContext, ex.getMessage());
        }

        return null;
    }

    private String handleFailure(Map<String, Object> requestContext, String errorDetails) {
        return caseManagementService.notifyBackOffice(requestContext, errorDetails);
    }
}