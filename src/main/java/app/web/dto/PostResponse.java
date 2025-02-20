package app.web.dto;


import app.like.model.LikeAct;
import app.post.model.PostType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private String content;
    private String imageUrl;
    private PostType postType;
    private LocalDateTime createdAt;
    private List<CommentResponse> comments;
    private String profilePicture;
   // private List<LikeAct> likes;
    private boolean isLikedByUser;
    private long likeCount;

        }
