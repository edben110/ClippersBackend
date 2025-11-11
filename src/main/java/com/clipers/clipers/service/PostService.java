package com.clipers.clipers.service;

import com.clipers.clipers.entity.Comment;
import com.clipers.clipers.entity.Post;
import com.clipers.clipers.entity.PostLike;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.CommentRepository;
import com.clipers.clipers.repository.PostLikeRepository;
import com.clipers.clipers.repository.PostRepository;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que implementa Mediator Pattern implícitamente
 * Coordina las interacciones entre posts, comentarios y likes
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public PostService(PostRepository postRepository,
                      CommentRepository commentRepository,
                      PostLikeRepository postLikeRepository,
                      UserRepository userRepository,
                      NotificationService notificationService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Template Method para creación de posts
     */
    public Post createPost(String userId, String content, String imageUrl, String videoUrl, Post.PostType type) {
        // Step 1: Validate user
        User user = validateAndGetUser(userId);
        
        // Step 2: Create post
        Post post = new Post(content, type, user);
        post.setImageUrl(imageUrl);
        post.setVideoUrl(videoUrl);
        
        // Step 3: Save post
        post = postRepository.save(post);
        
        // Step 4: Notify observers (if needed)
        // En este caso, no notificamos la creación de posts
        
        return post;
    }

    private User validateAndGetUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Mediator Pattern - coordina la acción de dar like
     */
    public void toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        
        User user = validateAndGetUser(userId);
        
        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingLike.isPresent()) {
            // Remove like
            postLikeRepository.delete(existingLike.get());
            post.setLikes(post.getLikes() - 1);
        } else {
            // Add like
            PostLike like = new PostLike(user, post);
            postLikeRepository.save(like);
            post.setLikes(post.getLikes() + 1);
            
            // Notify post owner (Observer pattern implícito)
            if (!post.getUser().getId().equals(userId)) {
                notificationService.notifyPostLiked(post.getUser().getId(), userId, postId);
            }
        }
        
        postRepository.save(post);
    }

    /**
     * Mediator Pattern - coordina la adición de comentarios
     */
    public Comment addComment(String postId, String userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        
        User user = validateAndGetUser(userId);
        
        Comment comment = new Comment(content, user, post);
        comment = commentRepository.save(comment);
        
        // Notify post owner (Observer pattern implícito)
        if (!post.getUser().getId().equals(userId)) {
            notificationService.notifyPostCommented(post.getUser().getId(), userId, postId, content);
        }
        
        return comment;
    }

    public Optional<Post> findById(String id) {
        return postRepository.findById(id);
    }

    public Page<Post> getFeed(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<Post> findByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Comment> getComments(String postId) {
        return commentRepository.findByPostId(postId);
    }

    public Page<Post> searchPosts(String query, Pageable pageable) {
        return postRepository.searchPosts(query, pageable);
    }

    public Post updatePost(String id, String content) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        post.setContent(content);
        return postRepository.save(post);
    }

    public void deletePost(String id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Publicación no encontrada");
        }
        postRepository.deleteById(id);
    }
}
