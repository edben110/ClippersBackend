package com.clipers.clipers.repository;

import com.clipers.clipers.entity.PostLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends MongoRepository<PostLike, String> {
    
    Optional<PostLike> findByUserIdAndPostId(String userId, String postId);
    
    List<PostLike> findByPostId(String postId);
    
    List<PostLike> findByUserId(String userId);
    
    boolean existsByUserIdAndPostId(String userId, String postId);
    
    Long countByPostId(String postId);
    
    void deleteByUserIdAndPostId(String userId, String postId);
}
