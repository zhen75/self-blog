package com.example.selfblog.post.po;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Post {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        draft,
        published,
    }

    private Status status;
}
