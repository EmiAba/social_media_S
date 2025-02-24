package app.comment.repository;

import app.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId);
    @Query("SELECT COUNT(c) FROM Comment c JOIN c.post p WHERE p.user.id = :userId AND c.createdAt >= :since")
    int countCommentsOnUserPostsSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    int countByPostId(@Param("postId") UUID postId);
}
