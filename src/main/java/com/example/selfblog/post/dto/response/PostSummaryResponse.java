package com.example.selfblog.post.dto.response;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
