package com.eleraky.studentexchange.dto.response;

// ============================================================
// @Builder: نمط Builder Pattern من Lombok
// يمكننا من إنشاء الكائن بطريقة جميلة:
// JwtResponse.builder().token("...").username("...").build();
// ============================================================
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JwtResponse: DTO لاستجابة تسجيل الدخول
 *
 * تحتوي على:
 * - token: JWT Token نفسه
 * - type: نوع التوكن (دائماً "Bearer")
 * - معلومات المستخدم الأساسية (بدون كلمة المرور!)
 *
 * Bearer: مصطلح قياسي في OAuth 2.0 و JWT
 * معناه: "حامل هذا التوكن له الحق في الوصول"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {


    // ============================================================
    // JWT Token: التذكرة الرقمية التي تثبت هوية المستخدم
    // شكل التوكن: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.rTCH8cLoGxAm_xw68z-zXVKi9ie6xJn9tnVWjd_9ftE
    // يتكون من 3 أجزاء مفصولة بنقطة:
    // Header.Payload.Signature
    // ============================================================
    private String token;

    // ============================================================
    // نوع التوكن: دائماً "Bearer"
    // هذا معيار RFC 6750 - The OAuth 2.0 Authorization Framework
    // ============================================================
    private String type = "Bearer";

    // معلومات المستخدم (بدون كلمة المرور)
    private Long userId;
    private String username;
    private String email;
    private String fullName;

    /**
     * Constructor مخصص (يدوي) للمرونة
     * يمكننا إنشاء الكائن بدون استخدام Builder
     */
    public JwtResponse(String token, Long userId, String username,
                       String email, String fullName) {
        this.token = token;
        this.type = "Bearer";
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

}
