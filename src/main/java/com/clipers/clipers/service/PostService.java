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
        validateAndGetUser(userId);
        
        // Step 2: Create post
        Post post = new Post(content, type, userId);
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
        
        validateAndGetUser(userId);
        
        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingLike.isPresent()) {
            // Remove like
            PostLike like = existingLike.get();
            postLikeRepository.delete(like);
            post.setLikes(post.getLikes() - 1);
            
            // Remove like ID from post's like list
            List<String> postLikeIds = post.getPostLikeIds();
            if (postLikeIds != null) {
                postLikeIds.remove(like.getId());
                post.setPostLikeIds(postLikeIds);
            }
        } else {
            // Add like
            PostLike like = new PostLike(userId, postId);
            PostLike savedLike = postLikeRepository.save(like);
            post.setLikes(post.getLikes() + 1);
            
            // Add like ID to post's like list
            List<String> postLikeIds = post.getPostLikeIds();
            if (postLikeIds == null) {
                postLikeIds = new java.util.ArrayList<>();
            }
            postLikeIds.add(savedLike.getId());
            post.setPostLikeIds(postLikeIds);
            
            // Notify post owner (Observer pattern implícito)
            if (!post.getUserId().equals(userId)) {
                notificationService.notifyPostLiked(post.getUserId(), userId, postId);
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
        
        validateAndGetUser(userId);
        
        Comment comment = new Comment(content, userId, postId);
        Comment savedComment = commentRepository.save(comment);
        
        // Add comment ID to post's comment list
        List<String> commentIds = post.getCommentIds();
        if (commentIds == null) {
            commentIds = new java.util.ArrayList<>();
        }
        commentIds.add(savedComment.getId());
        post.setCommentIds(commentIds);
        postRepository.save(post);
        
        // Populate user information
        userRepository.findById(savedComment.getUserId()).ifPresent(user -> {
            savedComment.setUser(user);
        });
        
        // Notify post owner (Observer pattern implícito)
        if (!post.getUserId().equals(userId)) {
            notificationService.notifyPostCommented(post.getUserId(), userId, postId, content);
        }
        
        return savedComment;
    }

    public Optional<Post> findById(String id) {
        return postRepository.findById(id);
    }

    public Page<Post> getFeed(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        // Populate user information for each post
        posts.forEach(post -> {
            userRepository.findById(post.getUserId()).ifPresent(user -> {
                post.setUser(user);
            });
        });
        return posts;
    }

    public List<Post> findByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Comment> getComments(String postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        // Populate user information for each comment
        comments.forEach(comment -> {
            userRepository.findById(comment.getUserId()).ifPresent(user -> {
                comment.setUser(user);
            });
        });
        return comments;
    }

    public Comment updateComment(String postId, String commentId, String userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        // Verify the comment belongs to the post
        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("El comentario no pertenece a esta publicación");
        }
        
        // Verify the user owns the comment
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar este comentario");
        }
        
        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);
        
        // Populate user information
        userRepository.findById(savedComment.getUserId()).ifPresent(user -> {
            savedComment.setUser(user);
        });
        
        return savedComment;
    }

    public void deleteComment(String postId, String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        // Verify the comment belongs to the post
        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("El comentario no pertenece a esta publicación");
        }
        
        // Get the post to check if user is the post owner
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        
        // Verify the user owns the comment OR owns the post
        if (!comment.getUserId().equals(userId) && !post.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }
        
        // Remove comment ID from post's comment list
        List<String> commentIds = post.getCommentIds();
        if (commentIds != null) {
            commentIds.remove(commentId);
            post.setCommentIds(commentIds);
            postRepository.save(post);
        }
        
        commentRepository.delete(comment);
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
        
        // Delete all comments associated with this post
        List<Comment> comments = commentRepository.findByPostId(id);
        commentRepository.deleteAll(comments);
        
        // Delete all likes associated with this post
        List<PostLike> likes = postLikeRepository.findByPostId(id);
        postLikeRepository.deleteAll(likes);
        
        // Delete the post
        postRepository.deleteById(id);
    }

    public int deleteVideoPostsByCompanies() {
        // Find all posts of type VIDEO or CLIPER
        List<Post> videoPosts = postRepository.findByType(Post.PostType.VIDEO);
        List<Post> cliperPosts = postRepository.findByType(Post.PostType.CLIPER);
        
        int deletedCount = 0;
        
        // Check each video post and delete if created by a company
        for (Post post : videoPosts) {
            Optional<User> userOpt = userRepository.findById(post.getUserId());
            if (userOpt.isPresent() && User.Role.COMPANY.equals(userOpt.get().getRole())) {
                deletePost(post.getId());
                deletedCount++;
            }
        }
        
        // Check each cliper post and delete if created by a company
        for (Post post : cliperPosts) {
            Optional<User> userOpt = userRepository.findById(post.getUserId());
            if (userOpt.isPresent() && User.Role.COMPANY.equals(userOpt.get().getRole())) {
                deletePost(post.getId());
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
}
