package app.web.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentModerationRequest {
    private UUID userId;
    private String content;
    private UUID postId;
}
