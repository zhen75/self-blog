package com.example.selfblog.post.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.selfblog.post.dto.response.PageResultResponse;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/articles")
public class PostController {
    private final PostService postService;
    private static final int PAGE_SIZE = 10;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PageResultResponse<PostSummaryResponse> getPosts(@RequestParam(defaultValue = "1") int page) {
        return postService.getPostsByPage(page, PAGE_SIZE);
    }

    @GetMapping("/{id}")
    public Post getById(@PathVariable Long id) {
        return postService.getPublishedById(id);
    }
}
