package com.example.gava.data;

import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        Boolean completed,
        String priority,
        LocalDateTime dueDate
) {
}
