-- name 컬럼에 NOT NULL 제약 조건 추가
ALTER TABLE todo_group MODIFY name VARCHAR(255) NOT NULL;

-- color 컬럼에 NOT NULL 제약 조건 추가
ALTER TABLE todo_group MODIFY color VARCHAR(7) NOT NULL;
