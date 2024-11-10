-- V2__Add_priority_and_due_date_to_todos.sql
ALTER TABLE todos ADD COLUMN priority ENUM('LOW', 'MEDIUM', 'HIGH');
ALTER TABLE todos ADD COLUMN due_date TIMESTAMP;