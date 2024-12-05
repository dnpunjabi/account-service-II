package com.example.account_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.example.account_service.enums.CustomerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrinocoCasePayload {
    // Fields extracted from Request Context
    private String transactionId;
    private String fkn;                 // Customer Identifier
    private String productCode;         // Product Code
    private CustomerType customerType;  // Customer type as an enum

    // Derived Fields
    private String caseId;              // Unique Case ID
    private LocalDateTime caseCreationTimestamp;  // Time when the case was created
    private String errorMessage;        // Error message describing the failure

    // Additional Constant Fields
    private String channel;             // Channel (e.g., DIGITAL_BANKING)
    private String severity;            // Severity of the case (e.g., HIGH, MEDIUM)

    // Additional Context (Debugging or Auditing)
    private Map<String, Object> additionalContext;

    /**
     * Factory method to create an OrinocoCasePayload from the request context and error message.
     * Dynamically maps the customer type and builds the payload.
     *
     * @param requestContext The request context containing all necessary fields.
     * @param errorMessage The error message describing the failure.
     * @return An OrinocoCasePayload instance populated with data.
     */
    public static OrinocoCasePayload createFromContext(Map<String, Object> requestContext, String errorMessage) {
        // Extract the CustomerType from the request context
        CustomerType customerType = (CustomerType) requestContext.get("customerType");

        return OrinocoCasePayload.builder()
            // Extract fields from the request context
            .transactionId((String) requestContext.get("transactionId"))
            .fkn((String) requestContext.get("fkn"))
            .productCode((String) requestContext.get("productCode"))
            .customerType(customerType)  // Use the CustomerType enum directly

            // Derived fields
            .caseId("CASE-" + System.currentTimeMillis())
            //.caseCreationTimestamp(LocalDateTime.now())
            .errorMessage(errorMessage)

            // Constant fields
            .channel("DIGITAL_BANKING")
            .severity(determineSeverity(requestContext))  // Updated severity logic

            // Additional context (e.g., full request context for debugging)
            .additionalContext(requestContext)
            .build();
    }

    /**
     * Determines the severity of the case based on the simulateFailure value.
     *
     * @param requestContext The request context containing relevant flags like simulateFailure.
     * @return The severity as a string (e.g., HIGH, MEDIUM, LOW).
     */
    private static String determineSeverity(Map<String, Object> requestContext) {
        // Fetch the simulateFailure value as a String
        String simulateFailure = (String) requestContext.getOrDefault("simulateFailure", "NONE");

        // Determine severity based on the failure type
        if ("NETWORK_ERROR".equalsIgnoreCase(simulateFailure) || "SERVICE_UNAVAILABLE".equalsIgnoreCase(simulateFailure)) {
            return "HIGH";
        } else if ("BAD_REQUEST".equalsIgnoreCase(simulateFailure)) {
            return "MEDIUM";
        } else {
            return "LOW";  // Default severity for no or unknown failures
        }
    }
}