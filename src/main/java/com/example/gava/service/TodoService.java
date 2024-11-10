package com.example.gava.service;

import com.example.gava.entity.Todo;
import com.example.gava.repository.TodoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Flux<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Flux<Todo> getAllTodosSortedByPriority() {
        return todoRepository.findAllByOrderByPriorityDesc();
    }

    public Flux<Todo> getAllTodosSortedByDueDate() {
        return todoRepository.findAllByOrderByDueDateAsc();
    }

    public Mono<Todo> getTodoById(Long id) {
        return todoRepository.findById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found")));
    }

    public Mono<Todo> createOrUpdate(Todo todo) {
        return todoRepository.save(todo);
    }

    public Mono<Void> deleteTodoById(Long id) {
        return todoRepository.deleteById(id);
    }

    public Flux<Todo> getTodosByDate(LocalDate date) {
        return todoRepository.findByDueDate(date);
    }

    public Flux<Todo> getTodosByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return todoRepository.findByDueDateBetween(startDate, endDate);
    }

    public Flux<Todo> getTodosByYear(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return todoRepository.findByDueDateBetween(startDate, endDate);
    }

    public Mono<Long> getCompletedTodoCountBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return todoRepository.countCompletedTodosBetween(startDate, endDate);
    }
}