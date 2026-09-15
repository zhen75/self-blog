package com.example.selfblog.post.utils;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.selfblog.post.po.Post;

import java.util.List;

@Mapper
public interface PostMapper {
        // 按照ID查询文章
        @Select("""
                        SELECT id, summary, title, content, created_at, status
                        FROM posts
                        WHERE id = #{id}
                        """)
        Post selectById(@Param("id") Long id);

        // 查询所有文章
        @Select("""
                        SELECT id, summary, title, content, created_at, status
                        FROM posts
                        ORDER BY created_at DESC
                        """)
        List<Post> selectAll();

        // 查询发布文章总数
        @Select("""
                        SELECT COUNT(*) FROM posts
                        """)
        int countAllPosts();

        // 查询某状态文章发布总数
        @Select("""
                        SELECT COUNT(*) FROM posts
                        WHERE status = #{status}
                        """)
        int countPostsByStatus(@Param("status") Post.Status status);

        // 分页查询已经发布文章
        @Select("""
                        SELECT id, summary, title, content, created_at, status
                                FROM posts
                                WHERE status = 'published'
                                ORDER BY created_at DESC
                                LIMIT #{offset}, #{limit}
                                    """)
        List<Post> selectByPublishedPage(@Param("offset") int offset, @Param("limit") int limit);

        // 按照标题查询文章
        @Select("""
                        SELECT id, summary, title, content, created_at, status
                        FROM posts
                        WHERE status = 'published'
                        AND title LIKE CONCAT('%', #{title}, '%')
                        """)
        List<Post> selectByPublishedTitle(@Param("title") String title);

        // 新增文章
        @Insert("""
                        INSERT INTO posts (title, summary, content)
                        VALUES (#{title},#{summary}, #{content})
                        """)
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Post post);

        // 修改文章
        @Update("""
                        UPDATE posts
                        SET title = #{title}, summary = #{summary}, content = #{content}
                        WHERE id = #{id}
                        """)
        int update(Post post);

        @Update("""
                        UPDATE posts
                        SET status = #{status}
                        WHERE id = #{id}
                        """)
        int updateStatus(@Param("id") Long id, @Param("status") Post.Status status);

        // 删除文章
        @Update("""
                        DELETE FROM posts
                        WHERE id = #{id}
                        """)
        int delete(@Param("id") Long id);

}
