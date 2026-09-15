package com.example.selfblog.post.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.selfblog.post.dto.request.CreatePostRequest;
import com.example.selfblog.post.dto.request.UpdatePostRequest;
import com.example.selfblog.post.dto.response.PostCreateResponse;
import com.example.selfblog.post.utils.PostService;
import com.example.selfblog.config.AuditLogService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminPostController {
    private final PostService postService;
    private final AuditLogService auditLogService;

    public AdminPostController(PostService postService, AuditLogService auditLogService) {
        this.postService = postService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public PostCreateResponse create(@Valid @RequestBody CreatePostRequest request) {
        PostCreateResponse response = postService.create(request);
        auditLogService.articleAction("create", response.id());
        return response;
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        postService.update(id, request);
        auditLogService.articleAction("update", id);
    }

    @PutMapping("/{id}/publish")
    public void publish(@PathVariable Long id) {
        postService.publish(id);
        auditLogService.articleAction("publish", id);
    }

    @PutMapping("/{id}/withdraw")
    public void withdraw(@PathVariable Long id) {
        postService.withdraw(id);
        auditLogService.articleAction("withdraw", id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
        auditLogService.articleAction("delete", id);
    }

}
