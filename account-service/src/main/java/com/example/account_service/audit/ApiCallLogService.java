package com.example.account_service.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiCallLogService {

    private final ApiCallLogRepository apiCallLogRepository;

    /**
     * Logs the API call response.
     *
     * @param featureName  The feature name (e.g., "schufa-check", "onboard").
     * @param fkn          The customer identifier.
     * @param productCode  The product code.
     * @param httpStatus   The HTTP status of the response.
     * @param responseBody The response body from the REST API call.
     */
    public void logApiResponse(String transactionId, String featureName, String fkn, String productCode,
                                String httpStatus, String requestPayload, String responseBody) {

        ApiCallLogEntity log = ApiCallLogEntity.builder()
                .transactionId(transactionId)
                .featureName(featureName)
                .fkn(fkn)
                .productCode(productCode)
                .httpStatus(httpStatus)
                .requestPayload(requestPayload) // Save request payload as JSON
                .responseBody(responseBody)     // Save response body as JSON
                .createdAt(LocalDateTime.now())
                .build();

        apiCallLogRepository.save(log);
    }

    /**
     * Finds all logs matching the provided filters.
     *
     * @param transactionId The transaction (optional).
     * @param featureName The feature name (optional).
     * @param fkn         The customer identifier (optional).
     * @param productCode The product code (optional).
     * @param fromDate    The start of the date range (optional).
     * @param toDate      The end of the date range (optional).
     * @return A list of matching logs.
     */
    public List<ApiCallLogEntity> findLogs(String transactionId, String featureName, String fkn, String productCode,
                                           LocalDateTime fromDate, LocalDateTime toDate) {

        Specification<ApiCallLogEntity> spec = Specification
                .where(ApiCallLogSpecifications.hasTransactionId(transactionId))
                .and(ApiCallLogSpecifications.hasFeatureName(featureName))
                .and(ApiCallLogSpecifications.hasFkn(fkn))
                .and(ApiCallLogSpecifications.hasProductCode(productCode))
                .and(ApiCallLogSpecifications.hasDateRange(fromDate, toDate));

        return apiCallLogRepository.findAll(spec);
    }
}