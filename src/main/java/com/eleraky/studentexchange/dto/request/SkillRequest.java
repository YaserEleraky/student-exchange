package com.eleraky.studentexchange.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {

    @NotBlank(message = "اسم المهارة مطلوب")
    @Size(min = 2, max = 100, message = "اسم المهارة يجب أن يكون بين 2 و 100 حرف")
    private String name;

    @Size(max = 50, message = "التصنيف لا يمكن أن يتجاوز 50 حرف")
    private String category;

    @Size(max = 1000, message = "الوصف لا يمكن أن يتجاوز 1000 حرف")
    private String description;

    // ============================================================
    // skillType: يجب أن يكون "TEACH" أو "LEARN"
    // level: يجب أن يكون "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"
    // سنتحقق من صحتها في الـ Service
    // ============================================================
    @NotBlank(message = "نوع المهارة مطلوب (TEACH أو LEARN)")
    private String skillType;

    @NotBlank(message = "مستوى المهارة مطلوب")
    private String skillLevel;
}