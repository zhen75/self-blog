package com.example.selfblog.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.selfblog.post.dto.request.CreatePostRequest;
import com.example.selfblog.post.dto.request.UpdatePostRequest;
import com.example.selfblog.post.dto.response.PageResultResponse;
import com.example.selfblog.post.dto.response.PostCreateResponse;
import com.example.selfblog.post.dto.response.PostSummaryResponse;
import com.example.selfblog.post.exception.IllegalPostStateException;
import com.example.selfblog.post.exception.PostNotFoundException;
import com.example.selfblog.post.po.Post;
import com.example.selfblog.post.utils.PostMapper;
import com.example.selfblog.post.utils.MarkdownService;
import com.example.selfblog.post.utils.PostService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostMapper postMapper;
    @Spy
    private MarkdownService markdownService = new MarkdownService();
    @InjectMocks
    private PostService postService;

    @Test
    void testCreate() {
        CreatePostRequest createPostRequest = new CreatePostRequest("test_title", "test_content");
        when(postMapper.insert(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(102L);
            return 1;
        });

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        PostCreateResponse res = postService.create(createPostRequest);
        verify(postMapper).insert(captor.capture());
        Post post = captor.getValue();

        assertEquals("test_title", post.getTitle());
        assertEquals("test_content", post.getContent());
        assertEquals(102L, res.id());
        verify(postMapper).insert(any(Post.class));
    }

    @Test
    void testGetById() {
        Post post = new Post();
        post.setTitle("this is test title");
        when(postMapper.selectById(3L)).thenReturn(post);

        Post result = postService.getById(3L);

        assertEquals("this is test title", result.getTitle());
        verify(postMapper).selectById(3L);
    }

    @Test
    void testGetPublishedById() {
        Post draft = new Post();
        Post publi = new Post();

        draft.setId(3L);
        draft.setStatus(Post.Status.draft);
        publi.setId(1L);
        publi.setStatus(Post.Status.published);

        when(postMapper.selectById(3L)).thenReturn(draft);
        when(postMapper.selectById(1L)).thenReturn(publi);

        assertThrows(PostNotFoundException.class, () -> postService.getPublishedById(3L));
        Post result_publi = postService.getPublishedById(1L);

        assertEquals(Post.Status.published, result_publi.getStatus());
        verify(postMapper).selectById(3L);
        verify(postMapper).selectById(1L);
    }

    @Test
    void testGetPostsByPage_first_page() {
        Post post1 = new Post();
        Post post2 = new Post();
        post1.setId(1L);
        post1.setTitle("first article");
        post1.setStatus(Post.Status.published);
        post2.setId(2L);
        post2.setTitle("second article");
        post2.setStatus(Post.Status.published);

        List<Post> posts = List.of(post1, post2);

        when(postMapper.countPostsByStatus(Post.Status.published)).thenReturn(2);

        // page =1,size =10
        // offset = (page - 1)*size;
        when(postMapper.selectByPublishedPage(0, 10)).thenReturn(posts);

        PageResultResponse<PostSummaryResponse> result = postService.getPostsByPage(1, 10);

        assertNotNull(result);
        assertEquals(2, result.posts().size());
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.total());

        verify(postMapper).countPostsByStatus(Post.Status.published);
        verify(postMapper).selectByPublishedPage(0, 10);
    }

    @Test
    void testGetPostsByPage_empty_page() {
        when(postMapper.countPostsByStatus(Post.Status.published)).thenReturn(0);
        when(postMapper.selectByPublishedPage(0, 10)).thenReturn(List.of());

        PageResultResponse<PostSummaryResponse> result = postService.getPostsByPage(1, 10);

        assertNotNull(result);
        assertNotNull(result.posts());
        assertEquals(0, result.posts().size());
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        verify(postMapper).countPostsByStatus(Post.Status.published);
        verify(postMapper).selectByPublishedPage(0, 10);

    }

    @Test
    void testUpdateSuccess() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("old title");
        post.setContent("old title");
        post.setStatus(Post.Status.draft);

        UpdatePostRequest request = new UpdatePostRequest("new title", "new content");

        when(postMapper.selectById(1L)).thenReturn(post);
        when(postMapper.update(any(Post.class))).thenReturn(1);

        postService.update(1L, request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).update(captor.capture());
        Post updatedPost = captor.getValue();

        assertEquals(1L, updatedPost.getId());
        assertEquals("new title", updatedPost.getTitle());
        assertEquals("new content", updatedPost.getContent());
        assertEquals(Post.Status.draft, updatedPost.getStatus());

        verify(postMapper).selectById(1L);
        verify(postMapper).update(any(Post.class));
    }

    @Test
    void testUpdateError() {
        Post post = new Post();
        post.setId(1L);
        post.setStatus(Post.Status.published);
        when(postMapper.selectById(1L)).thenReturn(post);

        UpdatePostRequest request = new UpdatePostRequest("new title", "new content");

        assertThrows(IllegalPostStateException.class, () -> postService.update(1L, request));
        verify(postMapper, never()).update(any(Post.class));
    }

    @Test
    void testPublish() {
        Post post = new Post();
        post.setId(1L);
        post.setStatus(Post.Status.draft);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(postMapper.updateStatus(1L, Post.Status.published)).thenReturn(1);

        postService.publish(1L);
        verify(postMapper).selectById(1L);
        verify(postMapper).updateStatus(1L, Post.Status.published);
    }

    @Test
    void testWithdraw() {
        Post post = new Post();
        post.setId(1L);
        post.setStatus(Post.Status.published);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(postMapper.updateStatus(1L, Post.Status.draft)).thenReturn(1);

        postService.withdraw(1L);
        verify(postMapper).selectById(1L);
        verify(postMapper).updateStatus(1L, Post.Status.draft);
    }

    @Test
    void testDelete(){
        Post post = new Post();
        post.setId(1L);
        post.setStatus(Post.Status.published);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(postMapper.delete(1L)).thenReturn(1);
        postService.delete(1L);
        verify(postMapper).delete(1L);
    }
}
