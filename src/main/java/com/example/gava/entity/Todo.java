package com.example.gava.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("todos")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    @Id
    private Long id;
    private String title;
    private Boolean completed = false;
    private Priority priority;
    private LocalDateTime dueDate;
}
