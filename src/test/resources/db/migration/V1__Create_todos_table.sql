CREATE TABLE todos (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       description VARCHAR(255) NOT NULL,
                       completed BOOLEAN DEFAULT FALSE
);