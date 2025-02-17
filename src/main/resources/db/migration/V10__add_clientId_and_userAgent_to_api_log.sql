ALTER TABLE api_logs
    ADD COLUMN client_ip VARCHAR(255),
    ADD COLUMN user_agent VARCHAR(255);