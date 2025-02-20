package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Size(min=4, message="Username have to be at least 4 symbols")
    private String username;
    @Size(min=4, message="Password have to be at least 4 symbols")
    private  String password;

}
