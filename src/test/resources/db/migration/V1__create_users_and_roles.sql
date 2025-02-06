-- V1__create_users_and_roles.sql

-- Users 테이블 생성
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 사용자 ID
                       username VARCHAR(255) NOT NULL UNIQUE,          -- 사용자 이름 (고유)
                       password VARCHAR(255) NOT NULL                  -- 비밀번호
);

-- User Roles 테이블 생성
CREATE TABLE user_roles (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 역할 ID
                            user_id BIGINT NOT NULL,                        -- 사용자 ID (외래 키)
                            role VARCHAR(255) NOT NULL,                     -- 사용자 역할
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);