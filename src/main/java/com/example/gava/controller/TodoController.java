package com.example.gava.controller;

import com.example.gava.entity.Todo;
import com.example.gava.service.TodoService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public Flux<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }

    @GetMapping("/sortedByPriority")
    public Flux<Todo> getAllTodosSortedByPriority() {
        return todoService.getAllTodosSortedByPriority();
    }

    @GetMapping("/sortedByDueDate")
    public Flux<Todo> getAllTodosSortedByDueDate() {
        return todoService.getAllTodosSortedByDueDate();
    }

    @GetMapping("/{id}")
    public Mono<Todo> getTodoById(@PathVariable Long id) {
        return todoService.getTodoById(id);
    }

    @PostMapping
    public Mono<Todo> createOrUpdate(@RequestBody Todo todo) {
        return todoService.createOrUpdate(todo);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteTodoById(@PathVariable Long id) {
        return todoService.deleteTodoById(id);
    }

    @GetMapping("/byDate")
    public Flux<Todo> getTodosByDate(@RequestParam("date") LocalDate date) {
        return todoService.getTodosByDate(date);
    }

    @GetMapping("/byMonth")
    public Flux<Todo> getTodosByMonth(@RequestParam("year") int year, @RequestParam("month") int month) {
        return todoService.getTodosByMonth(year, month);
    }

    @GetMapping("/byYear")
    public Flux<Todo> getTodosByYear(@RequestParam("year") int year) {
        return todoService.getTodosByYear(year);
    }

    @GetMapping("/api/todos/completed/count")
    public Mono<Long> getCompletedTodoCount(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        return todoService.getCompletedTodoCountBetween(startDate, endDate);
    }
}