package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.model.Post;
import app.post.service.PostService;
import app.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    @Transactional
    public Comment createComment(UUID postId, User user, String content) {
        Post post = postService.getPostEntityById(postId);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.getComments().add(savedComment);

        return savedComment;
    }

    public List<Comment> getCommentsForPost(UUID postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }
}