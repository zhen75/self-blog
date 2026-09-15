package com.example.selfblog.post.utils;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.selfblog.post.dto.request.CreatePostRequest;
import com.example.selfblog.post.dto.request.UpdatePostRequest;
import com.example.selfblog.post.dto.response.PageResultResponse;
import com.example.selfblog.post.dto.response.PostCreateResponse;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.exception.IllegalPostStateException;
import com.example.selfblog.post.exception.PostNotFoundException;
import com.example.selfblog.post.exception.PostPersistenceException;
import com.example.selfblog.post.po.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final int MAX_PAGE = 10_000;
    private final PostMapper postMapper;
    private final MarkdownService markdownService;

    /*
     * 管理端业务介绍：
     * 1.编辑文章。只有draft状态的文章可以编辑，编辑后仍然是draft状态。
     * 2.发布文章。只有draft状态的文章可以发布，发布后状态变为published。
     * 3.撤回文章。只有published状态的文章可以撤回，撤回后状态变为draft。
     * 4.删除文章。draft和published状态的文章都可以删除，删除后从数据库中删除。
     */

    // 根据标题和内容创建草稿并返回草稿ID
    public PostCreateResponse create(CreatePostRequest createPostRequest) {
        String summary = generateSummary(createPostRequest.content());
        Post post = PostConverter.toPO(createPostRequest, summary);
        int result = postMapper.insert(post);
        if (result != 1) {
            throw new PostPersistenceException("Failed to create post");
        }
        return new PostCreateResponse(post.getId());
    }

    // 根据ID获取文章
    public Post getById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new PostNotFoundException(id);
        }
        return post;
    }

    public List<Post> getAllPosts() {
        return postMapper.selectAll();
    }

    // 根据ID获取发布后文章
    public Post getPublishedById(Long id) {
        Post post = getById(id);
        if (post.getStatus() != Post.Status.published) {
            throw new PostNotFoundException(id);
        }
        return post;
    }

    public PageResultResponse<PostSummaryResponse> getPostsByPage(int page, int size) {
        if (page < 1 || page > MAX_PAGE || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be between 1 and 10000; size must be between 1 and 100");
        }
        int total = postMapper.countPostsByStatus(Post.Status.published);
        int offset = Math.multiplyExact(page - 1, size);
        List<Post> posts = postMapper.selectByPublishedPage(offset, size);
        var postSummaries = posts.stream()
                .map(PostConverter::toSummaryResponse)
                .toList();
        return new PageResultResponse<>(postSummaries, total, page, size);
    }

    // 修改草稿
    public void update(Long id, UpdatePostRequest updatePostRequest) {
        Post post = getById(id);
        if (post.getStatus() != Post.Status.draft) {
            throw new IllegalPostStateException("Only draft posts can be updated");
        }
        post.setTitle(updatePostRequest.title());
        post.setContent(updatePostRequest.content());
        post.setSummary(generateSummary(updatePostRequest.content()));
        int result = postMapper.update(post);
        if (result != 1) {
            throw new PostPersistenceException("Failed to update post");
        }
    }

    // 发布文章
    public void publish(Long id) {
        Post post = getById(id);
        if (post.getStatus() != Post.Status.draft) {
            throw new IllegalPostStateException("Only draft posts can be published");
        }
        int result = postMapper.updateStatus(id, Post.Status.published);
        if (result != 1) {
            throw new PostPersistenceException("Failed to publish post");
        }
    }

    // 撤回文章
    public void withdraw(Long id) {
        Post post = getById(id);
        if (post.getStatus() != Post.Status.published) {
            throw new IllegalPostStateException("Only published posts can be withdrawn");
        }
        int result = postMapper.updateStatus(id, Post.Status.draft);
        if (result != 1) {
            throw new PostPersistenceException("Failed to withdraw post");
        }
    }

    // 删除文章
    public void delete(Long id) {
        getById(id);
        int result = postMapper.delete(id);
        if (result != 1) {
            throw new PostPersistenceException("Failed to delete post");
        }
    }

    private String generateSummary(String content) {
        String plainText = markdownService.toPlainText(content);
        if (plainText.isBlank()) {
            return "";
        }
        return plainText.length() <= 255 ? plainText : plainText.substring(0, 255);
    }
}
