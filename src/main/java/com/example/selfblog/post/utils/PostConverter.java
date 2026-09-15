package com.example.selfblog.post.utils;

import com.example.selfblog.post.dto.request.CreatePostRequest;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.po.Post;

public final class PostConverter {

    public static Post toPO(CreatePostRequest createPostRequest, String summary) {
        Post post = new Post();
        post.setTitle(createPostRequest.title());
        post.setContent(createPostRequest.content());
        post.setSummary(summary);
        return post;
    }

    public static PostSummaryResponse toSummaryResponse(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

}
