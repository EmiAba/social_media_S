package app.post.Repository;

import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p WHERE p.user.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId) ORDER BY p.createdAt DESC")
    List<Post> findPostsByFollowedUsers(UUID userId);
}
