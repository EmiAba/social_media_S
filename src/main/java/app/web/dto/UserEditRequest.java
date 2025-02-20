package app.web.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEditRequest {

    @Size(max = 10, message = "First name can't have more than 10 symbols")
    private String firstName;

    @Size(max = 10, message = "Last name can't have more than 10 symbols")
    private String lastName;

    @URL(message = "Requires correct web link format")
    private String profilePicture;


    private String bio;
}