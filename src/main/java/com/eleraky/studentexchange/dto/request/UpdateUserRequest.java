package com.eleraky.studentexchange.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
}
