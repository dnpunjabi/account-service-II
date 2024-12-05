package com.example.account_service.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApiCallLogController {

    private final ApiCallLogService apiCallLogService;

    /**
     * Endpoint to retrieve API call logs based on filters.
     *
     * @param featureName The feature name (optional).
     * @param fkn         The customer identifier (optional).
     * @param productCode The product code (optional).
     * @param fromDate    The start of the date range (optional).
     * @param toDate      The end of the date range (optional).
     * @return A list of matching logs.
     */
    @GetMapping("/api/call-logs")
    public ResponseEntity<List<ApiCallLogEntity>> getCallLogs(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String featureName,
            @RequestParam(required = false) String fkn,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        List<ApiCallLogEntity> logs = apiCallLogService.findLogs(transactionId, featureName, fkn, productCode, fromDate, toDate);
        return ResponseEntity.ok(logs);
    }
}