package com.example.selfblog.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @NotBlank(message = "Title cannot be blank") @Size(max = 100, message = "Title cannot exceed 100 characters") String title,
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 200000, message = "Content cannot exceed 200000 characters") String content) {
}
