package com.example.selfblog.post.dto.response;

import java.util.List;

public record PageResultResponse<T>(
        List<T> posts,
        int total,
        int page,
        int size) {
}
