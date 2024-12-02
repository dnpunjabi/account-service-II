package com.example.account_service.enums;

public enum ProductType {
    BCA_CURRENT_ACCOUNT, // BCA - Current Account
    FGA_FLEX_ACCOUNT;    // FGA - Flex Account

    public static ProductType fromCode(String productCode) {
        return switch (productCode.toUpperCase()) {
            case "BCA" -> BCA_CURRENT_ACCOUNT;
            case "FGA" -> FGA_FLEX_ACCOUNT;
            default -> throw new IllegalArgumentException("Invalid product type code: " + productCode);
        };
    }
}
