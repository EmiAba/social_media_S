package app.like.service;

import app.exceprion.DomainException;
import app.like.model.LikeAct;
import app.like.repository.LikeActRepository;
import app.notification.service.NotificationService;
import app.post.Repository.PostRepository;
import app.post.model.Post;
import app.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LikeActService {
    private final LikeActRepository likeActRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public LikeActService(LikeActRepository likeActRepository,
                          PostRepository postRepository,
                          NotificationService notificationService) {
        this.likeActRepository = likeActRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public boolean toggleLike(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException("Post not found with id: " + postId));

        Optional<LikeAct> existingLike = likeActRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {

            likeActRepository.delete(existingLike.get());
            return false;
        } else {

            LikeAct likeAct = LikeAct.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeActRepository.save(likeAct);


            if (!post.getUser().getId().equals(user.getId())) {
                notificationService.createLikeNotification(
                        post.getUser(),
                        user.getUsername()
                );
            }
            return true;
        }
    }
    public boolean hasUserLikedPost(UUID postId, User user) {
        if (user == null) {
            return false;
        }

        return postRepository.findById(postId)
                .map(post -> likeActRepository.findByUserAndPost(user, post).isPresent())
                .orElse(false);
    }


    public long countLikes(UUID postId) {
        return postRepository.findById(postId)
                .map(likeActRepository::countByPost)
                .orElse(0L);
    }
}