package com.eleraky.studentexchange.dto.request;

// ============================================================
// jakarta.validation: حزمة التحقق من صحة البيانات
// @NotBlank: يمنع القيم الفارغة أو التي تحتوي على مسافات فقط
// مصدرها: Jakarta EE (المعيار الرسمي لجافا للمؤسسات)
// ============================================================
import jakarta.validation.constraints.NotBlank;

// ============================================================
// Lombok: لتقليل الكود المتكرر
// @Data = @Getter + @Setter + @ToString + @EqualsAndHashCode
// @NoArgsConstructor: Constructor فارغ (مطلوب لـ JSON deserialization)
// @AllArgsConstructor: Constructor بكل الحقول
// ============================================================
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest: DTO لطلب تسجيل الدخول
 *
 * لماذا نستخدم DTO منفصل للـ Login؟
 * 1. فصل بيانات تسجيل الدخول عن بيانات المستخدم الكاملة
 * 2. لا نحتاج email أو fullName لتسجيل الدخول
 * 3. يمكن إضافة validations خاصة بتسجيل الدخول فقط
 *
 * هذا المفهوم يسمى: Separation of Concerns (فصل الاهتمامات)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // ============================================================
    // @NotBlank:
    // - يمنع null
    // - يمنع السلسلة الفارغة ""
    // - يمنع السلاسل التي تحتوي على مسافات فقط "   "
    // message: رسالة الخطأ التي ستظهر للمستخدم
    // ============================================================
    @NotBlank(message = "UserName Required")
    private String username;

    @NotBlank(message = "Password Required")
    private String password;
}
