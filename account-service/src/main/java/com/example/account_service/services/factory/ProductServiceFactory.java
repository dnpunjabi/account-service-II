package com.example.account_service.services.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.account_service.enums.CustomerType;
import com.example.account_service.services.ProductService;
import com.example.account_service.services.impl.LegalEntityProductService;
import com.example.account_service.services.impl.NaturalPersonProductService;

@Component
public class ProductServiceFactory {

    private final NaturalPersonProductService naturalPersonService;
    private final LegalEntityProductService legalEntityService;

    @Autowired
    public ProductServiceFactory(NaturalPersonProductService naturalPersonService, LegalEntityProductService legalEntityService) {
        this.naturalPersonService = naturalPersonService;
        this.legalEntityService = legalEntityService;
    }

    public ProductService getService(CustomerType customerType) {
        switch (customerType) {
            case NATURAL_PERSON:
                return naturalPersonService;
            case LEGAL_ENTITY:
                return legalEntityService;
            default:
                throw new IllegalArgumentException("Unknown customer type: " + customerType);
        }
    }
}
