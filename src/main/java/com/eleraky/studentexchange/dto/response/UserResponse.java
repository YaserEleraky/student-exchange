package com.eleraky.studentexchange.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ⚠️ لا يوجد حقل password هنا - أمان!
}
