package com.clipers.clipers.repository;

import com.clipers.clipers.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    
    Optional<PostLike> findByUserIdAndPostId(String userId, String postId);
    
    List<PostLike> findByPostId(String postId);
    
    List<PostLike> findByUserId(String userId);
    
    boolean existsByUserIdAndPostId(String userId, String postId);
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    Long countLikesByPostId(@Param("postId") String postId);
    
    void deleteByUserIdAndPostId(String userId, String postId);
}
