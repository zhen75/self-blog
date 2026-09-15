package com.example.selfblog.post.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Post not found with ID: " + id);
    }

}
