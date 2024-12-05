package com.example.account_service.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.PostConstruct;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "account-service")
@Data
public class ProductConfig {

    @PostConstruct
public void printConfig() {
    System.out.println("Brands map: " + brands);
    if (brands != null) {
        System.out.println("Number of brands: " + brands.size());
        // Print details of the 'brands' map if it's not null
    }
}


    private Map<String, BrandConfig> brands;

    @Data
    public static class BrandConfig {
        @JsonProperty("products")
        private List<Product> products;
    }

    @Data
    public static class Product {
        @JsonProperty("name")
        private String name;        // Added name field
        @JsonProperty("productCode")
        private String productCode;
        @JsonProperty("features")
        private ProductFeatures features;
    }

    @Data
    public static class ProductFeatures {
        @JsonProperty("schufa")
        private boolean schufa;
        @JsonProperty("accountOpening")
        private boolean accountOpening;
        @JsonProperty("pinActivation")
        private boolean pinActivation;
        @JsonProperty("onlineBankingActivation")
        private OnlineBankingFeature onlineBankingActivation;
    }

    @Data
    public static class OnlineBankingFeature {
        @JsonProperty("enabled")
        private boolean enabled;
        @JsonProperty("subFeatures")
        private SubFeatures subFeatures;
    }

    @Data
    public static class SubFeatures {
        @JsonProperty("telephoneBanking")
        private boolean telephoneBanking;
        @JsonProperty("smsNotifications")
        private boolean smsNotifications;
        @JsonProperty("emailAlerts")
        private boolean emailAlerts;
    }
}
