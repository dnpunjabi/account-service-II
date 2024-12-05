package com.example.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    private String transactionId; // Added transaction ID
    private String fkn;           // Customer identifier
    private String productCode;   // Product code
    private String simulateFailure;
    private String failureTarget;
    private boolean pinSet;
    private boolean onlineBankingOptIn;
    private int customerType;     // 1 = Natural Person, 2 = Legal Entity
}