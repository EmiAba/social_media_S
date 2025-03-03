package app.follow.service;

import app.exceprion.DomainException;
import app.follow.model.Follow;
import app.follow.repository.FollowRepository;
import app.notification.service.NotificationService;
import app.user.model.User;
import app.user.repoistory.UserRepository;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Autowired
    public FollowService(FollowRepository followRepository, UserService userService, UserRepository userRepository, NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userService = userService;
        this.userRepository = userRepository;

        this.notificationService = notificationService;
    }

    public List<User> getSuggestedUsers(UUID userId) {
        List<User> allUsers = userService.getAllUsers();
        List<User> followedUsers = getFollowedUsers(userId);

        return allUsers.stream()
                .filter(user -> !followedUsers.contains(user) && !user.getId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<User> getFollowedUsers(UUID userId) {
        List<Follow> follows = followRepository.findByFollowerId(userId);
        return follows.stream()
                .map(Follow::getFollowed)
                .peek(user -> user.setOnline(userRepository.findById(user.getId()).get().isOnline()))
                .collect(Collectors.toList());
    }

    public void followUser(UUID followerId, UUID followedId) {
        User follower = userService.getUserById(followerId);
               ;

        User followed = userService.getUserById(followedId);


        if (followRepository.findByFollowerAndFollowed(follower, followed).isPresent()) {
            throw new DomainException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);


        notificationService.createFollowNotification(followed, follower.getUsername());

    }


    public void unfollowUser(UUID followerId, UUID followedId) {
        User follower = userService.getUserById(followerId);

        User followed = userService.getUserById(followedId);


        followRepository.findByFollowerAndFollowed(follower, followed)
                .ifPresent(followRepository::delete);
    }

    public boolean isFollowing(UUID followerId, UUID followedId) {
        User follower = userService.getUserById(followerId);
        User followed = userService.getUserById(followedId);

        return followRepository.findByFollowerAndFollowed(follower, followed).isPresent();
    }
}
