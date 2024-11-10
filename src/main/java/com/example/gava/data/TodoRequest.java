package com.example.gava.data;

import java.time.LocalDateTime;

public record TodoRequest(
        String title,
        Boolean completed,
        String priority,
        LocalDateTime dueDate
) {
}
