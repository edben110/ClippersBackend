package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    
    List<Comment> findByPostId(String postId);
    
    Page<Comment> findByPostIdOrderByCreatedAtAsc(String postId, Pageable pageable);
    
    List<Comment> findByUserId(String userId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Long countCommentsByPostId(@Param("postId") String postId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    Long countCommentsByUserId(@Param("userId") String userId);
}
