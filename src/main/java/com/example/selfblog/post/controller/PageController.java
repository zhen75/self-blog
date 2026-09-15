package com.example.selfblog.post.controller;

import java.util.List;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;

import com.example.selfblog.post.dto.response.PageResultResponse;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostService;
import com.example.selfblog.post.utils.MarkdownService;

@Controller
public class PageController {
    private static final int PAGE_SIZE = 6;
    private final PostService postService;
    private final MarkdownService markdownService;

    public PageController(PostService postService, MarkdownService markdownService) {
        this.postService = postService;
        this.markdownService = markdownService;
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "1") int page, Principal principal, Model model) {
        PageResultResponse<PostSummaryResponse> result = postService.getPostsByPage(page, PAGE_SIZE);
        int totalPages = Math.max(1, (int) Math.ceil((double) result.total() / PAGE_SIZE));
        model.addAttribute("result", result);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("isAuthenticated", principal != null);
        return "index";
    }

    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
        Post post = postService.getPublishedById(id);
        model.addAttribute("post", post);
        model.addAttribute("postHtml", markdownService.toHtml(post.getContent()));
        return "article-detail";
    }

    @GetMapping("/login")
    public String login(Principal principal, HttpSession session, Model model) {
        if (principal != null) {
            return "redirect:/admin";
        }
        Object loginError = session.getAttribute("LOGIN_ERROR");
        if (loginError != null) {
            model.addAttribute("loginError", loginError);
            session.removeAttribute("LOGIN_ERROR");
        }
        return "login";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) Post.Status status, Model model) {
        List<Post> allPosts = postService.getAllPosts();
        List<Post> visiblePosts = status == null
                ? allPosts
                : allPosts.stream().filter(post -> post.getStatus() == status).toList();
        model.addAttribute("posts", visiblePosts);
        model.addAttribute("activeStatus", status);
        model.addAttribute("totalCount", allPosts.size());
        model.addAttribute("draftCount", countByStatus(allPosts, Post.Status.draft));
        model.addAttribute("publishedCount", countByStatus(allPosts, Post.Status.published));
        return "admin";
    }

    @GetMapping("/admin/articles/new")
    public String createArticle(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("editing", false);
        return "article-form";
    }

    @GetMapping("/admin/articles/{id}/edit")
    public String editArticle(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getById(id));
        model.addAttribute("editing", true);
        return "article-form";
    }

    private long countByStatus(List<Post> posts, Post.Status status) {
        return posts.stream().filter(post -> post.getStatus() == status).count();
    }
}
