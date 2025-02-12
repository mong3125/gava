-- Icon 테이블 수정: base64_data 컬럼을 삭제하고 data 컬럼 추가 (MEDIUMBLOB 사용)
ALTER TABLE icon
    DROP COLUMN base64_data,
    ADD COLUMN data MEDIUMBLOB NOT NULL; -- 원본 아이콘 데이터 저장

-- SubTodo 테이블 수정: 시작 시간 및 종료 시간 컬럼 추가
ALTER TABLE sub_todo
    ADD COLUMN start_time TIME,       -- 시작 시간
    ADD COLUMN due_time TIME;         -- 종료 시간

-- Todo 테이블 수정: 그룹과의 Many-to-Many 관계 매핑 방식 개선
ALTER TABLE todo_group_mapping
    DROP COLUMN id; -- 기본 키 제거 (복합 키로 대체)

-- TodoGroupMapping 테이블 복합 키 설정 (todo_id와 group_id를 Primary Key로 설정)
ALTER TABLE todo_group_mapping
    ADD PRIMARY KEY (todo_id, group_id);

-- SubTodo 테이블: orphanRemoval 동작을 보장하기 위해 ON DELETE CASCADE 추가
-- 기존 외래 키를 삭제
ALTER TABLE sub_todo DROP FOREIGN KEY fk_sub_todo_todo;

-- 새로운 외래 키를 생성
ALTER TABLE sub_todo
    ADD CONSTRAINT fk_sub_todo_todo FOREIGN KEY (todo_id) REFERENCES todo(id) ON DELETE CASCADE;
