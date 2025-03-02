package app.user.repoistory;

import app.user.model.User;
import app.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    User findUserByRole(UserRole role);

   @Query("SELECT COUNT(u) FROM User u WHERE u.isOnline = true")
    long countOnlineUsers();




}
