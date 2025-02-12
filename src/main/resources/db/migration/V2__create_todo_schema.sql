-- Icon 테이블 생성
CREATE TABLE icon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,               -- 아이콘 이름
    base64_data TEXT NOT NULL,                -- Base64로 인코딩된 아이콘 데이터
    content_type VARCHAR(255) NOT NULL       -- MIME 타입 (예: image/png)
);

-- Todo 테이블 생성
CREATE TABLE todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,               -- 계획 이름
    date DATE NOT NULL,                       -- 날짜
    start_time TIME,                          -- 시작 시간
    due_time TIME,                            -- 종료 시간
    color VARCHAR(7),                         -- 색상 (Hex 코드)
    alarm_date_time DATETIME,                 -- 알람 시각
    is_important BOOLEAN DEFAULT FALSE,       -- 중요함 여부
    is_completed BOOLEAN DEFAULT FALSE,       -- 완료 여부
    icon_id BIGINT,                           -- 아이콘 참조
    user_id BIGINT NOT NULL,                  -- 사용자 참조
    CONSTRAINT fk_todo_icon FOREIGN KEY (icon_id) REFERENCES icon(id),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- SubTodo 테이블 생성
CREATE TABLE sub_todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    todo_id BIGINT NOT NULL,                  -- 부모 계획 참조
    name VARCHAR(255) NOT NULL,               -- 세부 계획 이름
    is_completed BOOLEAN DEFAULT FALSE,       -- 완료 여부
    CONSTRAINT fk_sub_todo_todo FOREIGN KEY (todo_id) REFERENCES todo(id)
);

-- TodoGroup 테이블 생성
CREATE TABLE todo_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,               -- 그룹 이름
    color VARCHAR(7),                         -- 그룹 색상 (Hex 코드)
    user_id BIGINT NOT NULL,                  -- 사용자 참조
    CONSTRAINT fk_todo_group_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- TodoGroupMapping 테이블 생성
CREATE TABLE todo_group_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    todo_id BIGINT,                           -- Todo 참조
    group_id BIGINT,                          -- 그룹 참조
    CONSTRAINT fk_todo_group_mapping_todo FOREIGN KEY (todo_id) REFERENCES todo(id),
    CONSTRAINT fk_todo_group_mapping_group FOREIGN KEY (group_id) REFERENCES todo_group(id)
);
