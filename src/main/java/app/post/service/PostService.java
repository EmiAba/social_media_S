package app.post.service;

import app.client.model.ModerationStatus;
import app.client.service.ModerationService;
import app.exceprion.DomainException;
import app.like.service.LikeActService;
import app.post.Repository.PostRepository;
import app.post.model.Post;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentResponse;
import app.web.dto.ContentModerationResponse;
import app.web.dto.PostRequest;
import app.web.dto.PostResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeActService likeActService;
    private final ModerationService moderationService;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService,
                       LikeActService likeActService, ModerationService moderationService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.likeActService = likeActService;
        this.moderationService = moderationService;
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
                .visible(false)
                .build();

        Post savedPost = postRepository.save(post);

        ContentModerationResponse moderationResponse =
                moderationService.submitForModeration(user.getId(), request.getContent(), savedPost.getId());

        if (moderationResponse != null && ModerationStatus.APPROVED.equals(moderationResponse.getStatus())) {
            savedPost.setVisible(true);
            savedPost = postRepository.save(savedPost);
        }

        return convertToResponse(savedPost, user);
    }

    public List<PostResponse> getAllPosts(User currentUser) {
        return postRepository.findByVisibleTrue()
                .stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(post -> convertToResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    public List<PostResponse> getUserPosts(User currentUser, UUID userId) {
        List<Post> userPosts;

        if (currentUser != null && currentUser.getId().equals(userId)) {
            userPosts = postRepository.findByUserId(userId);
        } else {
            userPosts = postRepository.findByUserIdAndVisibleTrue(userId);
        }

        return userPosts.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(post -> {
                    PostResponse response = convertToResponse(post, currentUser);

                    if (currentUser != null && currentUser.getId().equals(userId)) {
                        ContentModerationResponse moderationStatus =
                                moderationService.getPostModerationStatus(post.getId());

                        if (moderationStatus != null) {
                            response.setModerationStatus(moderationStatus.getStatus().toString());
                            response.setModerationReason(moderationStatus.getModerationReason());
                        }
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    public PostResponse getPostById(UUID postId, User currentUser) {
        Post post = getPostEntityById(postId);

        if (!post.isVisible() && (currentUser == null || !post.getUser().getId().equals(currentUser.getId()))) {
            throw new DomainException("Post is not available for viewing");
        }

        return convertToResponse(post, currentUser);
    }

    public PostResponse updatePost(UUID postId, PostRequest request, User currentUser) {
        Post post = getPostEntityById(postId);

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new DomainException("You can only update your own posts");
        }

        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setPostType(request.getPostType());
        post.setUpdatedAt(LocalDateTime.now());
        post.setVisible(false);

        Post updatedPost = postRepository.save(post);

        ContentModerationResponse moderationResponse =
                moderationService.submitForModeration(currentUser.getId(), request.getContent(), updatedPost.getId());

        if (moderationResponse != null && ModerationStatus.APPROVED.equals(moderationResponse.getStatus())) {
            updatedPost.setVisible(true);
            updatedPost = postRepository.save(updatedPost);
        }

        return convertToResponse(updatedPost, currentUser);
    }

    public void deletePost(UUID postId, User currentUser) {
        Post post = getPostEntityById(postId);

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new DomainException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    public List<PostResponse> getAdminAllPosts(User admin) {
        return postRepository.findAll()
                .stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(post -> {
                    PostResponse response = convertToResponse(post, admin);

                    ContentModerationResponse moderationStatus =
                            moderationService.getPostModerationStatus(post.getId());

                    if (moderationStatus != null) {
                        response.setModerationStatus(moderationStatus.getStatus().toString());
                        response.setModerationReason(moderationStatus.getModerationReason());
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    public void updatePostModerationStatus(UUID postId) {
        Post post = getPostEntityById(postId);
        boolean isApproved = moderationService.isPostApproved(postId);
        post.setVisible(isApproved);

        if (!isApproved) {
            ContentModerationResponse moderationStatus = moderationService.getPostModerationStatus(postId);
            if (moderationStatus != null) {
                post.setModerationReason(moderationStatus.getModerationReason());
            }
        }

        postRepository.save(post);
    }

    public PostResponse convertToResponse(Post post, User currentUser) {
        if (post == null) {
            return null;
        }

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

        long likeCount = likeActService.countLikes(post.getId());

        boolean isLikedByUser = false;
        if (currentUser != null) {
            isLikedByUser = likeActService.hasUserLikedPost(post.getId(), currentUser);
        }

        PostResponse.PostResponseBuilder responseBuilder = PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .postType(post.getPostType())
                .createdAt(post.getCreatedAt())
                .comments(commentResponses)
                .profilePicture(post.getUser().getProfilePicture())
                .likeCount(likeCount)
                .isLikedByUser(isLikedByUser)
                .visible(post.isVisible());

        if (post.getModerationReason() != null) {
            responseBuilder.moderationReason(post.getModerationReason());
        }

        return responseBuilder.build();
    }

    public PostResponse convertToResponse(Post post) {
        return convertToResponse(post, null);
    }

    public Post getPostEntityById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new DomainException("Post not found with id: " + id));
    }

    public List<Post> getPostsByUserId(UUID userId) {
        return postRepository.findByUserId(userId);
    }
}