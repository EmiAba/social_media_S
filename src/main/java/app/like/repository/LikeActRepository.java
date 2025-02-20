package app.like.repository;

import app.like.model.LikeAct;
import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface LikeActRepository extends JpaRepository<LikeAct, UUID> {
    long countByPost(Post post);
    Optional<LikeAct> findByUserAndPost(User user, Post post);

}
