package com.example.selfblog.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.selfblog.post.dto.response.PostCreateResponse;
import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostMapper;

import tools.jackson.databind.ObjectMapper;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PostMapper postMapper;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(username = "wan", roles = "ADMIN")
        void testPublishPost() throws Exception {
                Post post = new Post();
                post.setTitle("test title");
                post.setContent("test content");

                postMapper.insert(post);

                Long id = post.getId();

                Post before = postMapper.selectById(id);
                assertEquals(Post.Status.draft, before.getStatus());

                mockMvc.perform(
                                put("/api/admin/articles/{id}/publish", id)
                                                .with(testSecurityContext())
                                                .with(csrf()))
                                .andExpect(status().isOk());

                Post after = postMapper.selectById(id);

                assertEquals(
                                Post.Status.published,
                                after.getStatus());
        }

        @Test
        @WithMockUser(username = "wan", roles = "ADMIN")
        void testCreatePost() throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/admin/articles")
                                                .with(testSecurityContext())
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "test title",
                                                                    "content": "test content"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andReturn();

                String json = result.getResponse().getContentAsString();

                PostCreateResponse response = objectMapper.readValue(
                                json,
                                PostCreateResponse.class);

                Long id = response.id();

                Post saved = postMapper.selectById(id);

                assertNotNull(saved);
                assertEquals("test title", saved.getTitle());
                assertEquals("test content", saved.getContent());
                assertEquals(Post.Status.draft, saved.getStatus());
        }
}
