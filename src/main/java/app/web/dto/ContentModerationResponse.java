package app.web.dto;


import app.client.model.ModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentModerationResponse {
    private UUID id;
    private UUID postId;
    private String content;
    private ModerationStatus status;
    private LocalDateTime createdOn;
    private String moderationReason;
    private UUID userId;
}