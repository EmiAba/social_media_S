package app.like.repository;

import app.like.model.LikeAct;
import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface LikeActRepository extends JpaRepository<LikeAct, UUID> {
   // long countByPost(Post post);
    Optional<LikeAct> findByUserAndPost(User user, Post post);
    @Query("SELECT COUNT(l) FROM LikeAct l JOIN l.post p WHERE p.user.id = :userId AND p.createdAt >= :since")
    int countLikesReceivedByUserSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(l) FROM LikeAct l WHERE l.post.id = :postId")
    int countByPostId(@Param("postId") UUID postId);

}
