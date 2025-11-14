package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    List<Post> findByUserId(String userId);
    
    Page<Post> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Post> findByType(Post.PostType type);
    
    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    Page<Post> searchPosts(String query, Pageable pageable);
    
    Page<Post> findAllByOrderByLikesDescCreatedAtDesc(Pageable pageable);
    
    Long countByUserId(String userId);
}
