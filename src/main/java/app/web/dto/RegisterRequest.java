package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequest {

    @Size(min=4, message="Username have to be at least 4 symbols")
    private String username;

    @NotNull
    @Email
    private String email;
    @Size(min=4, message="Password have to be at least 4 symbols")
    private String password;



}