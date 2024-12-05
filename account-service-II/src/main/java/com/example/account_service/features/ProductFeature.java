package com.example.account_service.features;

import java.util.Map;

public interface ProductFeature {
    void execute(Map<String, Object> requestContext) throws Exception;
}