package com.example.account_service.enums;

import java.util.Arrays;

public enum CustomerType {
    NATURAL_PERSON(1),   // Individual
    LEGAL_ENTITY(3);     // Corporate

    private final int code;

    CustomerType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // Method to get CustomerType by code
    public static CustomerType fromCode(int code) {
        return Arrays.stream(values())
                .filter(customerType -> customerType.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer type code: " + code));
    }
}