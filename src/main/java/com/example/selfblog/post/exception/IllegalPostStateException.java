package com.example.selfblog.post.exception;

public class IllegalPostStateException extends RuntimeException {
    public IllegalPostStateException(String message) {
        super(message);
    }

}
