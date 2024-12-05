package com.example.account_service.features.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.account_service.config.ProductConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FeatureOrderBuilder {

    private final ProductConfig productConfig;

    @Autowired
    public FeatureOrderBuilder(ProductConfig productConfig) {
        this.productConfig = productConfig;
    }

    /**
     * Builds the ordered list of features for execution based on product configuration priorities.
     *
     * @param product The product configuration.
     * @return A list of features in the correct execution order.
     */
    public List<String> buildFeatureOrder(ProductConfig.Product product) {
        List<FeatureWithPriority> featuresWithPriorities = new ArrayList<>();

        log.info("Building feature execution order for product: {}", product.getProductCode());

        ProductConfig.ProductFeatures features = product.getFeatures();

        // Add features to the list with their priorities if they are enabled
        if (features.isPinActivationEnabled()) {
            featuresWithPriorities.add(new FeatureWithPriority("PINActivation", features.getPinActivation().getPriority()));
        }
        if (features.isAccountOpeningEnabled()) {
            featuresWithPriorities.add(new FeatureWithPriority("AccountOpening", features.getAccountOpening().getPriority()));
        }
        if (features.isSchufaEnabled()) {
            featuresWithPriorities.add(new FeatureWithPriority("Schufa", features.getSchufa().getPriority()));
        }
        if (features.isOnlineBankingEnabled()) {
            featuresWithPriorities.add(new FeatureWithPriority("OnlineBankingActivation", features.getOnlineBankingActivation().getPriority()));
        }

        // Sort the features by priority
        List<String> executionOrder = featuresWithPriorities.stream()
            .sorted((f1, f2) -> Integer.compare(f1.getPriority(), f2.getPriority())) // Sort by priority
            .map(FeatureWithPriority::getName) // Extract feature names
            .collect(Collectors.toList());

        log.info("Execution order for product {}: {}", product.getProductCode(), executionOrder);
        return executionOrder;
    }

    /**
     * Retrieves the OnlineBankingFeature subfeatures for the specified brand and product code.
     *
     * @param brand       The brand name (e.g., "BrandA").
     * @param productCode The product code (e.g., "BCA").
     * @return A map of subfeatures and their respective enabled/disabled statuses.
     */
    public Map<String, Boolean> getOnlineBankingAttributes(String brand, String productCode) {
        log.info("Fetching Online Banking subfeatures for brand: {}, productCode: {}", brand, productCode);

        // Retrieve the product configuration
        ProductConfig.Product product = productConfig.getBrands().get(brand).getProducts().stream()
            .filter(p -> p.getProductCode().equalsIgnoreCase(productCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found for brand: " + brand));

        // Retrieve the OnlineBankingFeature
        ProductConfig.OnlineBankingFeature onlineBankingFeature = product.getFeatures().getOnlineBankingActivation();

        // Check if OnlineBankingFeature is present
        if (onlineBankingFeature == null) {
            log.info("Online Banking Activation is not enabled for brand: {}, productCode: {}", brand, productCode);
            return null; // Return null or empty map if not enabled
        }

        // Dynamically add subfeatures
        Map<String, Boolean> onlineBankingAttributes = new LinkedHashMap<>();
        ProductConfig.SubFeatures subFeatures = onlineBankingFeature.getSubFeatures();
        if (subFeatures != null) {
            onlineBankingAttributes.put("telephoneBanking", subFeatures.isTelephoneBanking());
            onlineBankingAttributes.put("smsNotifications", subFeatures.isSmsNotifications());
            onlineBankingAttributes.put("emailAlerts", subFeatures.isEmailAlerts());
        }

        log.info("Online Banking Attributes for productCode {}: {}", productCode, onlineBankingAttributes);
        return onlineBankingAttributes;
    }

    // Inner class to hold feature name and priority
    private static class FeatureWithPriority {
        private final String name;
        private final int priority;

        public FeatureWithPriority(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }
    }
}