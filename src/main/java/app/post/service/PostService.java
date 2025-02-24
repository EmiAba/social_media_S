package app.post.service;

import app.exceprion.DomainException;
import app.like.service.LikeActService;
import app.post.Repository.PostRepository;
import app.post.model.Post;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentResponse;
import app.web.dto.PostRequest;
import app.web.dto.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeActService likeActService;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService, LikeActService likeActService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.likeActService = likeActService;
    }

    public PostResponse createPost(PostRequest request, User user) {
        Post post = Post.builder()
                .user(user)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .postType(request.getPostType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .build();

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost, user);
    }

    public List<PostResponse> getAllPosts(User currentUser) {
        return postRepository.findAll()
                .stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(post -> convertToResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    public PostResponse convertToResponse(Post post, User currentUser) {
        if (post == null) {
            return null;
        }

        // Convert comments
        List<CommentResponse> commentResponses = (post.getComments() != null)
                ? post.getComments().stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .username(comment.getUser().getUsername())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .profilePicture(comment.getUser().getProfilePicture())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        // Get the current like count directly from database
        long likeCount = likeActService.countLikes(post.getId());

        // Check if current user has liked this post
        boolean isLikedByUser = false;
        if (currentUser != null) {
            isLikedByUser = likeActService.hasUserLikedPost(post.getId(), currentUser);
        }

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .postType(post.getPostType())
                .createdAt(post.getCreatedAt())
                .comments(commentResponses)
                .profilePicture(post.getUser().getProfilePicture())
                .likeCount(likeCount)          // Add current like count
                .isLikedByUser(isLikedByUser)  // Add user's like status
                .build();
    }


    public PostResponse convertToResponse(Post post) {
        return convertToResponse(post, null);
    }

    public Post getPostEntityById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new DomainException("Post not found with id: " + id));
    }
}