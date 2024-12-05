package com.example.account_service.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "account-service")
public class ProductConfig {

    private Map<String, BrandConfig> brands;

    @Data
    public static class BrandConfig {
        private List<Product> products;
    }

    @Data
    public static class Product {
        private String name;
        private String productCode;
        private ProductFeatures features;
    }

    @Data
    public static class ProductFeatures {
        private FeatureConfig schufa;
        private FeatureConfig accountOpening;
        private FeatureConfig pinActivation;
        private OnlineBankingFeature onlineBankingActivation;

        // Utility methods to check if a feature is enabled
        public boolean isSchufaEnabled() {
            return schufa != null;
        }

        public boolean isAccountOpeningEnabled() {
            return accountOpening != null;
        }

        public boolean isPinActivationEnabled() {
            return pinActivation != null;
        }

        public boolean isOnlineBankingEnabled() {
            return onlineBankingActivation != null;
        }
    }

    @Data
    public static class FeatureConfig {
        private int priority;
    }

    @Data
    public static class OnlineBankingFeature {
        private int priority;
        private SubFeatures subFeatures;
    }

    @Data
    public static class SubFeatures {
        private boolean telephoneBanking;
        private boolean smsNotifications;
        private boolean emailAlerts;
    }
}