package app.client.model;

import app.web.dto.ContentModerationRequest;
import app.web.dto.ContentModerationResponse;
import app.web.dto.ModerationDecisionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "moderation-service", url = "http://localhost:8081/moderation-svc/api/v1")
public interface ModerationServiceClient {
    @GetMapping("/moderation/test")
    String testEndpoint();

    @PostMapping("/moderation/posts")
    ContentModerationResponse submitPost(@RequestBody ContentModerationRequest request);

    @GetMapping("/moderation/posts/original/{postId}")
    ContentModerationResponse getPostByOriginalId(@PathVariable("postId") UUID postId);

    @GetMapping("/moderation/posts/{postId}")
    ContentModerationResponse getPost(@PathVariable("postId") UUID postId);

    @GetMapping("/moderation/posts/user/{userId}")
    List<ContentModerationResponse> getUserPosts(@PathVariable("userId") UUID userId);

    @GetMapping("/moderation/posts/pending")
    List<ContentModerationResponse> getPendingPosts();

    @GetMapping("/moderation/posts/history")
    List<ContentModerationResponse> getAllModerationHistory();

    @GetMapping("/moderation/posts/history/status/{status}")
    List<ContentModerationResponse> getModerationHistoryByStatus(@PathVariable("status") ModerationStatus status);

    @PostMapping("/moderation/moderate")
    ContentModerationResponse moderatePost(@RequestBody ModerationDecisionRequest request);
}