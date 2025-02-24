package app.follow.repository;

import app.follow.model.Follow;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerAndFollowed(User follower, User followed);

    List<Follow> findByFollowerId(UUID userId);
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId AND f.followedAt > :since")
    int countNewFollowers(@Param("userId") UUID userId, @Param("since") LocalDateTime since);


}
