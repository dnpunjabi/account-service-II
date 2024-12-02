package com.example.account_service.features.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.account_service.features.ProductFeature;
import com.example.account_service.features.impl.AccountOpeningFeatureImpl;
import com.example.account_service.features.impl.OnlineBankingActivationFeatureImpl;
import com.example.account_service.features.impl.PinActivationFeatureImpl;
import com.example.account_service.features.impl.SchufaCheckFeatureImpl;

@Component
public class ProductFeatureFactory {

    private final AccountOpeningFeatureImpl accountOpeningFeature;
    private final PinActivationFeatureImpl pinActivationFeature;
    private final OnlineBankingActivationFeatureImpl onlineBankingFeature;
    private final SchufaCheckFeatureImpl schufaCheckFeature;

    @Autowired
    public ProductFeatureFactory(AccountOpeningFeatureImpl accountOpeningFeature,
                                 PinActivationFeatureImpl pinActivationFeature,
                                 OnlineBankingActivationFeatureImpl onlineBankingFeature,
                                 SchufaCheckFeatureImpl schufaCheckFeature) {
        this.accountOpeningFeature = accountOpeningFeature;
        this.pinActivationFeature = pinActivationFeature;
        this.onlineBankingFeature = onlineBankingFeature;
        this.schufaCheckFeature = schufaCheckFeature;
    }

    public ProductFeature getFeature(String featureType) {
        switch (featureType) {
            case "account-opening":
                return accountOpeningFeature;
            case "activate-pin":
                return pinActivationFeature;
            case "activate-online-banking":
                return onlineBankingFeature;
            case "schufa-check":
                return schufaCheckFeature;
            default:
                throw new IllegalArgumentException("Unknown feature type: " + featureType);
        }
    }
}