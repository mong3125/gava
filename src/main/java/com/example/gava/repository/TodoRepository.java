package com.example.gava.repository;

import com.example.gava.entity.Todo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TodoRepository extends ReactiveCrudRepository<Todo, Long> {
    Flux<Todo> findAllByOrderByPriorityAsc();
    Flux<Todo> findAllByOrderByPriorityDesc();
    Flux<Todo> findAllByOrderByDueDateAsc();

    @Query("SELECT * FROM todos WHERE DATE(due_date) = :dueDate")
    Flux<Todo> findByDueDate(LocalDate dueDate);
    Flux<Todo> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(*) FROM todos WHERE completed = true AND due_date BETWEEN :startDate AND :endDate")
    Mono<Long> countCompletedTodosBetween(LocalDateTime startDate, LocalDateTime endDate);
}
