package app.scheduler;

import app.comment.repository.CommentRepository;
import app.follow.repository.FollowRepository;
import app.like.repository.LikeActRepository;
import app.post.Repository.PostRepository;
import app.post.model.Post;
import app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EngagementSummaryService {

    private final FollowRepository followRepository;
    private final LikeActRepository likeActRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public String generateEngagementSummary(User user) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);


        int newFollowers = followRepository.countNewFollowers(user.getId(), oneWeekAgo);
        Post mostLikedPost = postRepository.findMostLikedPost(user.getId(), oneWeekAgo);
        Post mostCommentedPost = postRepository.findMostCommentedPost(user.getId(), oneWeekAgo);


        int totalPosts = postRepository.countPostsByUserSince(user.getId(), oneWeekAgo);
        int totalLikes = likeActRepository.countLikesReceivedByUserSince(user.getId(), oneWeekAgo);
        int totalComments = commentRepository.countCommentsOnUserPostsSince(user.getId(), oneWeekAgo);


        int mostLikedPostLikes = 0;
        int mostCommentedPostComments = 0;

        if (mostLikedPost != null) {
            mostLikedPostLikes = likeActRepository.countByPostId(mostLikedPost.getId());
        }

        if (mostCommentedPost != null) {
            mostCommentedPostComments = commentRepository.countByPostId(mostCommentedPost.getId());
        }


        return String.format(
                "📊 **Your Weekly Engagement Summary** 📊\n\n" +
                        "👥 **New Followers**: %d\n" +
                        "📝 **Posts This Week**: %d\n" +
                        "❤️ **Total Likes Received**: %d\n" +
                        "💬 **Total Comments Received**: %d\n\n" +
                        "🌟 **Most Popular Content** 🌟\n" +
                        "❤️ **Most Liked Post**: \"%s\" (%d likes)\n" +
                        "💬 **Most Commented Post**: \"%s\" (%d comments)\n\n" +
                        "Keep engaging and stay active! 🚀",
                newFollowers,
                totalPosts,
                totalLikes,
                totalComments,
                (mostLikedPost != null ? summarizePost(mostLikedPost) : "No posts this week"),
                mostLikedPostLikes,
                (mostCommentedPost != null ? summarizePost(mostCommentedPost) : "No posts this week"),
                mostCommentedPostComments
        );
    }


    private String summarizePost(Post post) {
        if (post == null) {
            return "No post";
        }

        String content = post.getContent();
        if (content == null || content.isEmpty()) {
            return "Post #" + post.getId();
        }


        if (content.length() > 50) {
            return content.substring(0, 47) + "...";
        }

        return content;
    }
}