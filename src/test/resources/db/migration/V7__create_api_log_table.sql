CREATE TABLE api_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_uri VARCHAR(255) NOT NULL,
    method VARCHAR(255) NOT NULL,
    request_param VARCHAR(2000) NOT NULL,
    status INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    execution_time BIGINT NOT NULL
);