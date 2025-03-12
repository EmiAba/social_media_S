package app.client.service;

import app.client.model.ModerationServiceClient;
import app.client.model.ModerationStatus;
import app.web.dto.ContentModerationRequest;
import app.web.dto.ContentModerationResponse;
import app.web.dto.ModerationDecisionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ModerationServiceClient moderationClient;

    public ContentModerationResponse submitForModeration(UUID userId, String content, UUID postId) {
        ContentModerationRequest request = new ContentModerationRequest(userId, content, postId);
        return moderationClient.submitPost(request);
    }

    public ContentModerationResponse getPostModerationStatus(UUID postId) {
        return moderationClient.getPostByOriginalId(postId);
    }

    public boolean isPostApproved(UUID postId) {
        ContentModerationResponse response = moderationClient.getPostByOriginalId(postId);
        return response != null && ModerationStatus.APPROVED.equals(response.getStatus());
    }

    public List<ContentModerationResponse> getPendingPosts() {
        return moderationClient.getPendingPosts();
    }

    public List<ContentModerationResponse> getUserModerationHistory(UUID userId) {
        return moderationClient.getUserPosts(userId);
    }

    public List<ContentModerationResponse> getAllModerationHistory() {
        return moderationClient.getAllModerationHistory();
    }

    public ContentModerationResponse approvePost(UUID postId) {
        ModerationDecisionRequest request = new ModerationDecisionRequest();
        request.setPostId(postId);
        request.setApproved(true);
        request.setReason(null);

        return moderationClient.moderatePost(request);
    }

    public ContentModerationResponse rejectPost(UUID postId, String reason) {
        ModerationDecisionRequest request = new ModerationDecisionRequest();
        request.setPostId(postId);
        request.setApproved(false);
        request.setReason(reason);

        return moderationClient.moderatePost(request);
    }

    public List<ContentModerationResponse> getFilteredPendingPosts(String userId, String dateFrom, String dateTo) {
        List<ContentModerationResponse> pendingPosts = getPendingPosts();

        if (userId != null && !userId.isEmpty()) {
            pendingPosts = filterByUserId(pendingPosts, userId);
        }

        pendingPosts = applyDateFilters(pendingPosts, dateFrom, dateTo);
        pendingPosts = sortByCreationDateDesc(pendingPosts);

        return pendingPosts;
    }

    public List<ContentModerationResponse> getFilteredModerationHistory(String status, String dateFrom, String dateTo) {
        List<ContentModerationResponse> moderationHistory = getAllModerationHistory();

        if (status != null && !status.isEmpty()) {
            moderationHistory = filterByStatus(moderationHistory, status);
        }

        moderationHistory = applyDateFilters(moderationHistory, dateFrom, dateTo);
        moderationHistory = sortByCreationDateDesc(moderationHistory);

        return moderationHistory;
    }



    private List<ContentModerationResponse> filterByUserId(List<ContentModerationResponse> posts, String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            return posts.stream()
                    .filter(post -> userUuid.equals(post.getUserId()))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return posts;
        }
    }

    private List<ContentModerationResponse> filterByStatus(List<ContentModerationResponse> posts, String status) {
        ModerationStatus statusEnum = ModerationStatus.valueOf(status);
        return posts.stream()
                .filter(item -> item.getStatus() == statusEnum)
                .collect(Collectors.toList());
    }

    private List<ContentModerationResponse> applyDateFilters(List<ContentModerationResponse> posts, String dateFrom, String dateTo) {
        List<ContentModerationResponse> result = posts;

        if (dateFrom != null && !dateFrom.isEmpty()) {
            LocalDate fromDate = LocalDate.parse(dateFrom);
            result = result.stream()
                    .filter(post -> post.getCreatedOn() != null &&
                            !post.getCreatedOn().toLocalDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            LocalDate toDate = LocalDate.parse(dateTo);
            LocalDate nextDay = toDate.plusDays(1);
            result = result.stream()
                    .filter(post -> post.getCreatedOn() != null &&
                            post.getCreatedOn().toLocalDate().isBefore(nextDay))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private List<ContentModerationResponse> sortByCreationDateDesc(List<ContentModerationResponse> posts) {
        return posts.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedOn() == null) return 1;
                    if (b.getCreatedOn() == null) return -1;
                    return b.getCreatedOn().compareTo(a.getCreatedOn());
                })
                .collect(Collectors.toList());
    }
}