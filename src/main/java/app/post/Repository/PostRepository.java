package app.post.Repository;

import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {


    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.createdAt >= :since " +
            "ORDER BY (SELECT COUNT(l) FROM LikeAct l WHERE l.post = p) DESC LIMIT 1")
    Post findMostLikedPost(@Param("userId") UUID userId, @Param("since") LocalDateTime since);


    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.createdAt >= :since " +
            "ORDER BY (SELECT COUNT(c) FROM Comment c WHERE c.post = p) DESC LIMIT 1")
    Post findMostCommentedPost(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.createdAt >= :since")
    int countPostsByUserSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    List<Post> findByUserId(UUID userId);



    List<Post> findByVisibleTrue();


    List<Post> findByUserIdAndVisibleTrue(UUID userId);
}






