package com.example.gava.integration;

import com.example.gava.entity.Priority;
import com.example.gava.entity.Todo;
import com.example.gava.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TodoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        r2dbcEntityTemplate.getDatabaseClient()
                .sql("TRUNCATE TABLE todos;")
                .fetch()
                .rowsUpdated()
                .then(
                        r2dbcEntityTemplate.getDatabaseClient()
                                .sql("ALTER TABLE todos AUTO_INCREMENT = 1;")
                                .fetch()
                                .rowsUpdated()
                )
                .block(); // 비동기 작업을 블로킹하여 완료되도록 대기)

        // 테스트 데이터 초기화
        todoRepository.deleteAll()
                .thenMany(Flux.fromIterable(Arrays.asList(
                        new Todo(null, "Task 1", false, Priority.HIGH, now.plusDays(1)),
                        new Todo(null, "Task 2", true, Priority.MEDIUM, now.plusDays(2)),
                        new Todo(null, "Task 3", false, Priority.LOW, now.plusDays(3))
                )))
                .flatMap(todoRepository::save)
                .blockLast();
    }

    @Test
    void getAllTodos() {
        webTestClient.get()
                .uri("/todos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .hasSize(3);
    }

    @Test
    void getTodoById() {
        webTestClient.get()
                .uri("/todos/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .value(todo -> {
                    assertThat(todo.getId()).isEqualTo(1L);
                    assertThat(todo.getTitle()).isEqualTo("Task 1");
                });
    }

    @Test
    void getAllTodosSortedByPriority() {
        webTestClient.get()
                .uri("/todos/sortedByPriority")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .value(todos -> {
                    assertThat(todos.get(0).getPriority()).isEqualTo(Priority.HIGH);
                    assertThat(todos.get(1).getPriority()).isEqualTo(Priority.MEDIUM);
                    assertThat(todos.get(2).getPriority()).isEqualTo(Priority.LOW);
                });
    }

    @Test
    void getAllTodosSortedByDueDate() {
        webTestClient.get()
                .uri("/todos/sortedByDueDate")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .value(todos -> {
                    assertThat(todos.get(0).getDueDate()).isBefore(todos.get(1).getDueDate());
                    assertThat(todos.get(1).getDueDate()).isBefore(todos.get(2).getDueDate());
                });
    }

    @Test
    void createTodo() {
        Todo newTodo = new Todo(null, "New Task", false, Priority.HIGH, now.plusDays(4));

        webTestClient.post()
                .uri("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newTodo), Todo.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .value(todo -> {
                    assertThat(todo.getId()).isNotNull();
                    assertThat(todo.getTitle()).isEqualTo("New Task");
                });
    }

    @Test
    void updateTodo() {
        Todo updatedTodo = new Todo(1L, "Updated Task 1", true, Priority.LOW, now.plusDays(5));

        webTestClient.post()
                .uri("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedTodo), Todo.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .value(todo -> {
                    assertThat(todo.getId()).isEqualTo(1L);
                    assertThat(todo.getTitle()).isEqualTo("Updated Task 1");
                    assertThat(todo.getCompleted()).isTrue();
                    assertThat(todo.getPriority()).isEqualTo(Priority.LOW);
                });
    }

    @Test
    void deleteTodo() {
        webTestClient.delete()
                .uri("/todos/1")
                .exchange()
                .expectStatus().isOk();

        // 삭제 확인
        webTestClient.get()
                .uri("/todos/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTodosByDate() {
        LocalDate testDate = LocalDate.from(now.plusDays(1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/todos/byDate")
                        .queryParam("date", testDate.toString())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .value(todos -> {
                    assertThat(todos).hasSize(1);
                    assertThat(todos.get(0).getTitle()).isEqualTo("Task 1");
                });
    }

    @Test
    void getTodosByMonth() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/todos/byMonth")
                        .queryParam("year", now.getYear())
                        .queryParam("month", now.getMonthValue())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .value(todos -> {
                    assertThat(todos).hasSize(3);
                });
    }

    @Test
    void getTodosByYear() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/todos/byYear")
                        .queryParam("year", now.getYear())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class)
                .value(todos -> {
                    assertThat(todos).hasSize(3);
                });
    }
}