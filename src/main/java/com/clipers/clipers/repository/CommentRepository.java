package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    List<Comment> findByPostId(String postId);
    
    Page<Comment> findByPostIdOrderByCreatedAtAsc(String postId, Pageable pageable);
    
    List<Comment> findByUserId(String userId);
    
    Long countByPostId(String postId);
    
    Long countByUserId(String userId);
}
