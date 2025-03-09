package app.client.model;

import app.web.dto.ContentModerationRequest;
import app.web.dto.ContentModerationResponse;
import app.web.dto.ModerationDecisionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "moderation-service", url = "${moderation.service.url}")
public interface ModerationServiceClient {
    @GetMapping("/api/v1/moderation/test")
    String testEndpoint();

    @PostMapping("/api/v1/moderation/posts")
    ContentModerationResponse submitPost(@RequestBody ContentModerationRequest request);

    @GetMapping("/api/v1/moderation/posts/original/{postId}")
    ContentModerationResponse getPostByOriginalId(@PathVariable("postId") UUID postId);

    @GetMapping("/api/v1/moderation/posts/{postId}")
    ContentModerationResponse getPost(@PathVariable("postId") UUID postId);

    @GetMapping("/api/v1/moderation/posts/user/{userId}")
    List<ContentModerationResponse> getUserPosts(@PathVariable("userId") UUID userId);

    @GetMapping("/api/v1/moderation/posts/pending")
    List<ContentModerationResponse> getPendingPosts();

    @GetMapping("/api/v1/moderation/posts/history")
    List<ContentModerationResponse> getAllModerationHistory();

    @GetMapping("/api/v1/moderation/posts/history/status/{status}")
    List<ContentModerationResponse> getModerationHistoryByStatus(@PathVariable("status") ModerationStatus status);

    @PostMapping("/api/v1/moderation/moderate")
    ContentModerationResponse moderatePost(@RequestBody ModerationDecisionRequest request);



}