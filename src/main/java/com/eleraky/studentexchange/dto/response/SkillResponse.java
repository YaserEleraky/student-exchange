package com.eleraky.studentexchange.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private Long id;
    private String name;
    private String category;
    private String description;
    private String skillType;     // "TEACH" أو "LEARN"
    private String level;         // "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"

    // ============================================================
    // معلومات المستخدم الذي أضاف المهارة
    // نرسل فقط المعلومات الأساسية - ليس كل بيانات المستخدم
    // ============================================================
    private Long userId;
    private String username;

    private LocalDateTime createdAt;
}