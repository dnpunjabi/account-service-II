package com.example.account_service.enums;

public enum Brand {
    BRANDA,
    BRANDB,
    BRANDC;

    public static Brand fromString(String brandName) {
        try {
            return Brand.valueOf(brandName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid brand name: " + brandName);
        }
    }
}