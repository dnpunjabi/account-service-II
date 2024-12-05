package com.example.account_service.audit;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class ApiCallLogSpecifications {

    public static Specification<ApiCallLogEntity> hasTransactionId(String transactionId) {
        return (root, query, criteriaBuilder) ->
            transactionId == null || transactionId.isEmpty()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("transactionId"), transactionId);
    }

    public static Specification<ApiCallLogEntity> hasFeatureName(String featureName) {
        return (root, query, criteriaBuilder) ->
            featureName == null || featureName.isEmpty()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("featureName"), featureName);
    }

    public static Specification<ApiCallLogEntity> hasFkn(String fkn) {
        return (root, query, criteriaBuilder) ->
            fkn == null || fkn.isEmpty()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("fkn"), fkn);
    }

    public static Specification<ApiCallLogEntity> hasProductCode(String productCode) {
        return (root, query, criteriaBuilder) ->
            productCode == null || productCode.isEmpty()
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("productCode"), productCode);
    }

    public static Specification<ApiCallLogEntity> hasDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return (root, query, criteriaBuilder) -> {
            Predicate greaterThanOrEqualTo = fromDate == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
            Predicate lessThanOrEqualTo = toDate == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate);
            return criteriaBuilder.and(greaterThanOrEqualTo, lessThanOrEqualTo);
        };
    }
}