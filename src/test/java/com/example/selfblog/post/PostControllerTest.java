package com.example.selfblog.post;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.example.selfblog.exception.GlobalExceptionHandler;
import com.example.selfblog.post.controller.PostController;
import com.example.selfblog.post.dto.response.PageResultResponse;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.exception.PostNotFoundException;
import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostService;

import java.util.List;

@WebMvcTest(PostController.class)
@Import(GlobalExceptionHandler.class)
public class PostControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PostService postService;

        @Test
        void testGetPost() throws Exception {
                Post post = new Post();
                post.setId(1L);
                post.setTitle("Test");
                post.setStatus(Post.Status.published);

                when(postService.getPublishedById(1L)).thenReturn(post);
                when(postService.getPublishedById(100L)).thenThrow(new PostNotFoundException(100L));

                mockMvc.perform(get("/api/articles/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Test"))
                                .andExpect(jsonPath("$.status").value("published"));

                mockMvc.perform(get("/api/articles/100")).andExpect(status().isNotFound());

                verify(postService).getPublishedById(1L);

        }

        @Test
        void testGetPosts() throws Exception {
                PostSummaryResponse p1 = new PostSummaryResponse(1L, "first title", null, null, null);
                PostSummaryResponse p2 = new PostSummaryResponse(12L, "twelve title", null, null, null);

                List<PostSummaryResponse> psr = List.of(p1, p2);
                PageResultResponse<PostSummaryResponse> response = new PageResultResponse<>(
                                psr,
                                34,
                                1,
                                10);
                when(postService.getPostsByPage(1, 10)).thenReturn(response);

                mockMvc.perform(get("/api/articles")
                                .param("page", "1"))
                                .andExpect(status().isOk())

                                .andExpect(jsonPath("$.total").value(34))
                                .andExpect(jsonPath("$.page").value(1))
                                .andExpect(jsonPath("$.size").value(10))

                                .andExpect(jsonPath("$.posts").isArray())
                                .andExpect(jsonPath("$.posts.length()").value(2))

                                .andExpect(jsonPath("$.posts[0].id").value(1))
                                .andExpect(jsonPath("$.posts[0].title").value("first title"))

                                .andExpect(jsonPath("$.posts[1].id").value(12))
                                .andExpect(jsonPath("$.posts[1].title").value("twelve title"));

                verify(postService).getPostsByPage(1, 10);
        }

}
