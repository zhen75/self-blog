package com.example.selfblog.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostMapper;

import java.util.List;

@SpringBootTest
@Transactional
public class PostMapperTest {

    @Autowired
    private PostMapper postMapper;

    @Test
    void testInsert() {
        Post post = new Post();
        post.setTitle("Test Title");
        post.setContent("Test Content");
        int result = postMapper.insert(post);

        assertEquals(1, result);
        assertNotNull(post.getId());
    }

    @Test
    @Sql("/post-test-data.sql")
    void testSelectById() {
        Post post = postMapper.selectById(1L);
        assertNotNull(post);
        assertEquals("Post 1", post.getTitle());
        assertEquals("Summary 1", post.getSummary());
        assertEquals("Content 1", post.getContent());
        assertEquals(Post.Status.published, post.getStatus());
    }

    @Test
    @Sql("/post-test-data.sql")
    void testSelectAll() {
        List<Post> posts = postMapper.selectAll();

        Post post_9 = posts.get(8);
        assertEquals("Post 9", post_9.getTitle());
        assertEquals("Summary 9", post_9.getSummary());
        assertEquals("Content 9", post_9.getContent());
        assertEquals(Post.Status.published, post_9.getStatus());
        assertEquals(12, posts.size());

    }

    @Test
    @Sql ("/post-test-data.sql")
    void testCountAllPosts() {
        int count = postMapper.countAllPosts();
        assertEquals(12, count);
    }

    @Test
    @Sql ("/post-test-data.sql")
    void testCountPostsByStatus() {
        int countPublished = postMapper.countPostsByStatus(Post.Status.published);
        int countDraft = postMapper.countPostsByStatus(Post.Status.draft);
        assertEquals(6, countPublished);
        assertEquals(6, countDraft);
    }

    @Test
    @Sql ("/post-test-data.sql")
    void testSelectByPublishedPage() {
        List<Post> postsPage1 = postMapper.selectByPublishedPage(0, 4);
        List<Post> postsPage2 = postMapper.selectByPublishedPage(4, 4);

        assertEquals(4, postsPage1.size());
        assertEquals(2, postsPage2.size());
    }

    @Test
    @Sql ("/post-test-data.sql")
    void testSelectByPublishedTitle() {
        List<Post> posts1 = postMapper.selectByPublishedTitle("Post 1");
        List<Post> posts2 = postMapper.selectByPublishedTitle("Post");
        List<Post> posts3 = postMapper.selectByPublishedTitle("Non-existing title");
        assertEquals(2, posts1.size());
        assertEquals("Post 1", posts1.get(0).getTitle());
        assertEquals(6, posts2.size());
        assertEquals(0, posts3.size());
    }

    @Test
    @Sql ("/post-test-data.sql")
    void testUpdate() {
        Post post = postMapper.selectById(1L);
        post.setTitle("Updated Title");
        post.setContent("Updated Content");
        int result = postMapper.update(post);
        assertEquals(1, result);
        assertEquals("Updated Title", postMapper.selectById(1L).getTitle());
        assertEquals("Updated Content", postMapper.selectById(1L).getContent());
    }

    @Test
    @Sql ("/post-test-data.sql")
    void testDelete() {
        assertNotNull(postMapper.selectById(2L));
        int result = postMapper.delete(2L);
        assertNull(postMapper.selectById(2L));
        assertEquals(1, result);
    }
}
