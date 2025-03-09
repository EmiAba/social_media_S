package app.web.dto;

import java.util.UUID;

public class ModerationDecisionRequest {
    private UUID postId;
    private boolean approved;
    private String reason;


    public ModerationDecisionRequest() {
    }


    public ModerationDecisionRequest(UUID postId, boolean approved, String reason) {
        this.postId = postId;
        this.approved = approved;
        this.reason = reason;
    }


    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ModerationDecisionRequest{" +
                "postId=" + postId +
                ", approved=" + approved +
                ", reason='" + reason + '\'' +
                '}';
    }
}