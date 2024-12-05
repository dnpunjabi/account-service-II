CREATE TABLE api_call_logs (
    id SERIAL PRIMARY KEY,                              -- Auto-incrementing primary key
    transaction_id VARCHAR(255) NOT NULL,                -- Unique transaction ID
    feature_name VARCHAR(100) NOT NULL,                 -- Feature name, e.g., schufa-check
    fkn VARCHAR(100) NOT NULL,                          -- Customer identifier
    product_code VARCHAR(50) NOT NULL,                  -- Product code
    http_status VARCHAR(255) NOT NULL,                   -- HTTP status of the response
    request_payload JSONB,                              -- Request payload stored as JSONB
    response_body JSONB,                                -- Response body stored as JSONB
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- Timestamp when the record was created
);