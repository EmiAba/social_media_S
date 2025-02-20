package app.like.service;

import app.exceprion.DomainException;
import app.like.model.LikeAct;
import app.like.repository.LikeActRepository;
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

    public LikeActService(LikeActRepository likeActRepository, PostRepository postRepository) {
        this.likeActRepository = likeActRepository;
        this.postRepository = postRepository;
    }


    @Transactional
    public boolean toggleLike(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException("Post not found with id: " + postId));

        Optional<LikeAct> existingLike = likeActRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // Unlike the post
            likeActRepository.delete(existingLike.get());
            return false;
        } else {
            // Like the post
            LikeAct likeAct = LikeAct.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeActRepository.save(likeAct);
            return true;
        }
    }


    public long countLikes(UUID postId) {
        return postRepository.findById(postId)
                .map(likeActRepository::countByPost)
                .orElse(0L);
    }


    public boolean hasUserLikedPost(UUID postId, User user) {
        return postRepository.findById(postId)
                .map(post -> likeActRepository.findByUserAndPost(user, post).isPresent())
                .orElse(false);
    }


    public Map<UUID, Long> getLikeCountsForPosts(List<UUID> postIds) {
        List<Post> posts = postRepository.findAllById(postIds);
        Map<UUID, Long> likeCounts = new HashMap<>();

        for (Post post : posts) {
            likeCounts.put(post.getId(), likeActRepository.countByPost(post));
        }

        return likeCounts;
    }
}