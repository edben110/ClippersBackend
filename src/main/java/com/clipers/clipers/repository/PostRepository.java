package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    
    List<Post> findByUserId(String userId);
    
    Page<Post> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Post> findByType(Post.PostType type);
    
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likes DESC, p.createdAt DESC")
    Page<Post> findPostsOrderByPopularity(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countPostsByUserId(@Param("userId") String userId);
}
