package com.eleraky.studentexchange.model;

// ============================================================
// JPA: لتعريف الكيان والعلاقات مع قاعدة البيانات
// ============================================================
import jakarta.persistence.*;

// ============================================================
// Validation: للتحقق من صحة البيانات قبل حفظها
// ============================================================
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ============================================================
// Lombok: لتقليل الكود المتكرر
// ============================================================
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============================================================
// Jackson: للتحكم في JSON serialization
// @JsonIgnore: يمنع إرسال حقل معين في JSON (نتجنب الـ recursion)
// ============================================================
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    // لا يمكن إدخال قيمة غير موجودة في الـ Enum
    // ============================================================
    /**
     * نوع المهارة:
     * TEACH = المستخدم يريد تعليم هذه المهارة للآخرين
     * LEARN = المستخدم يريد تعلم هذه المهارة من الآخرين
     */
    public enum SkillType {
        TEACH,  // أنا أعلم هذه المهارة
        LEARN   // أنا أريد تعلم هذه المهارة
    }

    /**
     * مستوى المهارة:
     * BEGINNER = مبتدئ
     * INTERMEDIATE = متوسط
     * ADVANCED = متقدم
     * EXPERT = خبير
     */
    public enum SkillLevel {
        BEGINNER,      // مبتدئ
        INTERMEDIATE,  // متوسط
        ADVANCED,      // متقدم
        EXPERT         // خبير
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Skill name is required")
    @Size(min = 2,max = 255, message = "Skill name must be less than 255 characters")
    @Column(nullable = false,length = 100)
    private String name;

    // ============================================================
    // تصنيف المهارة: "برمجة", "لغات", "تصميم", "موسيقى", "رياضة"
    // ============================================================
    @Size(max = 50, message = "التصنيف لا يمكن أن يتجاوز 50 حرف")
    @Column(length = 50)
    private String category;

    // ============================================================
    // وصف المهارة: تفاصيل إضافية عن المهارة
    // ============================================================
    @Size(max = 1000, message = "الوصف لا يمكن أن يتجاوز 1000 حرف")
    @Column(length = 1000)
    private String description;

    // ============================================================
    // @Enumerated(EnumType.STRING): تخزين الـ Enum كنص في قاعدة البيانات
    // وليس كرقم (0, 1, 2)
    // EnumType.ORDINAL: يخزن كرقم (غير مفضل)
    // EnumType.STRING: يخزن كالنص "TEACH", "LEARN" (مفضل)
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skillType;  // هل المستخدم يعلم أم يتعلم هذه المهارة؟

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skillLevel;

    // ============================================================
    // @ManyToOne: علاقة Many-to-One
    // المهارة الواحدة تنتمي لمستخدم واحد (User)
    // المستخدم الواحد يمكنه إضافة مهارات كثيرة
    //
    // FetchType.LAZY: لا نجلب بيانات المستخدم إلا إذا طلبناها
    // (تحسين للأداء - لا نحمل بيانات لا نحتاجها)
    //
    // @JoinColumn: يحدد اسم العمود الذي سيكون Foreign Key
    // name = "user_id": اسم العمود في جدول skills
    // nullable = false: لا يمكن أن تكون المهارة بدون مستخدم
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // ============================================================
    // تاريخ إنشاء المهارة
    // updatable = false: لا يمكن تغيير هذا الحقل بعد الإنشاء
    // ============================================================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ============================================================
    // @PrePersist: ينفذ قبل الحفظ في قاعدة البيانات
    // نعين تاريخ الإنشاء تلقائياً
    // ============================================================
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
