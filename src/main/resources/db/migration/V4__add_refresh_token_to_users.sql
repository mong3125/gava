-- users 테이블에 refresh_token 컬럼 추가
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(255) NULL;