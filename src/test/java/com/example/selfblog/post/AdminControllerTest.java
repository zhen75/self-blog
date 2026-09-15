package com.example.selfblog.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.example.selfblog.exception.GlobalExceptionHandler;
import com.example.selfblog.config.AuditLogService;
import com.example.selfblog.post.controller.AdminPostController;
import com.example.selfblog.post.dto.request.CreatePostRequest;
import com.example.selfblog.post.dto.request.UpdatePostRequest;
import com.example.selfblog.post.dto.response.PostCreateResponse;
import com.example.selfblog.post.utils.PostService;

import org.springframework.http.MediaType;

@WebMvcTest(AdminPostController.class)
@Import(GlobalExceptionHandler.class)
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void testCreate() throws Exception {
        PostCreateResponse response = new PostCreateResponse(2L);

        when(postService.create(any(CreatePostRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/admin/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "create title",
                                    "content": "create content"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(postService).create(any(CreatePostRequest.class));
    }

    @Test
    void testUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/articles/{id}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "update title",
                            "content": "update content"
                        }
                        """))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdatePostRequest> captor = ArgumentCaptor.forClass(UpdatePostRequest.class);

        verify(postService).update(eq(2L), captor.capture());

        UpdatePostRequest request = captor.getValue();

        assertEquals("update title", request.title());
        assertEquals("update content", request.content());
    }

    @Test
    void testPublish() throws Exception {

        mockMvc.perform(
                put("/api/admin/articles/{id}/publish", 2L))
                .andExpect(status().isOk());

        verify(postService).publish(2L);
    }

    @Test
    void testWithdraw() throws Exception {

        mockMvc.perform(
                put("/api/admin/articles/{id}/withdraw", 2L))
                .andExpect(status().isOk());

        verify(postService).withdraw(2L);
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/admin/articles/{id}", 1L))
                .andExpect(status().isOk());

        verify(postService).delete(1L);
    }

}
