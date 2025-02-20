package app.web.dto;

import app.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MessageRequest {



    @NotBlank(message = "Receiver username cannot be empty")
    private String username;

    @NotBlank(message = "Message content cannot be empty")
    private String content;


    private LocalDateTime timestamp;

    private boolean isRead;

}
