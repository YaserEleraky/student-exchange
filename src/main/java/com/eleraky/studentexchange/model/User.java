package com.eleraky.studentexchange.model;

// ============================================================
// لماذا نستخدم هذه الـ imports؟
// ============================================================
// jakarta.persistence.* : هي حزمة JPA (Java Persistence API)
// تُستخدم لتعريف الكيانات (Entities) التي ستتحول إلى جداول في قاعدة البيانات
// JPA هي مواصفة (Specification) و Hibernate هو التنفيذ (Implementation)

import jakarta.persistence.*;
// jakarta.validation.* : حزمة للتحقق من صحة البيانات قبل حفظها
// تمنع إدخال بيانات خاطئة مثل: username فارغ أو email بدون @

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok: مكتبة لتقليل الكود المتكرر (boilerplate code)
// بدلاً من كتابة getters, setters, constructors يدوياً
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// لاستخدام التواريخ والأوقات
import java.time.LocalDateTime;

// ============================================================
// @Entity: تخبر JPA أن هذه الفئة هي كيان (Entity)
// أي أنها ستصبح جدولاً في قاعدة البيانات
// ============================================================
@Entity
// ============================================================
// @Table: نحدد اسم الجدول في قاعدة البيانات
// لو لم نستخدمها، سيكون اسم الجدول هو نفس اسم الفئة (user)
// ============================================================
@Table(name = "users")  // اخترنا "users" لأن "user" كلمة محجوزة في بعض قواعد البيانات

// ============================================================
// @Data: من Lombok - تنشئ تلقائياً:
// - Getters لجميع الحقول
// - Setters لجميع الحقول
// - toString()
// - equals() و hashCode()
// ============================================================
@Data

// ============================================================
// @NoArgsConstructor: من Lombok - تنشئ Constructor فارغ (بدون وسائط)
// هذا مطلوب من JPA لإنشاء كائنات جديدة
// ============================================================
@NoArgsConstructor

// ============================================================
// @AllArgsConstructor: من Lombok - تنشئ Constructor بكل الحقول
// مفيد لإنشاء كائن بسرعة
// ============================================================
@AllArgsConstructor
public class User {
    // ============================================================
    // @Id: هذا الحقل هو المفتاح الأساسي (Primary Key)
    // كل جدول في قاعدة البيانات يجب أن يكون له مفتاح أساسي
    // ============================================================
    // ============================================================
    // @GeneratedValue: نحدد كيفية إنشاء قيمة الـ ID تلقائياً
    // GenerationType.IDENTITY: نترك قاعدة البيانات هي التي تولد الـ ID
    // PostgreSQL ستستخدم SERIAL أو BIGSERIAL تلقائياً
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // @NotBlank: من Validation - يمنع القيمة الفارغة أو التي تحتوي على مسافات فقط
    // message: رسالة الخطأ التي ستظهر للمستخدم إذا كانت القيمة فارغة
    // ============================================================
    // ============================================================
    // @Size: يحدد الطول الأدنى والأقصى للنص
    // min = 4: على الأقل 4 أحرف (لتجنب الأسماء القصيرة جداً)
    // max = 50: على الأكثر 50 حرف (لحجم معقول في قاعدة البيانات)
    // ============================================================
    // ============================================================
    // @Column: إعدادات العمود في قاعدة البيانات
    // nullable = false: لا يمكن أن يكون فارغاً في قاعدة البيانات (NOT NULL)
    // unique = true: لا يمكن تكرار القيمة (UNIQUE CONSTRAINT)
    // length = 50: طول العمود في قاعدة البيانات
    // ============================================================

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // ============================================================
    // @Email: يتحقق من صحة صيغة البريد الإلكتروني
    // يجب أن تحتوي على @ ونطاق صحيح مثل example@domain.com
    // ============================================================

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email and Email should be in format user@example.com")
    @Column(nullable = false, unique = true,  length = 100)
    private String email;

    // OAuth2 users have no password — nullable
    @Size(max = 120)
    @Column(nullable = true, length = 120)
    private String password;

    // ============================================================
    // fullName: الاسم الكامل للمستخدم
    // name = "full_name": اسم العمود في قاعدة البيانات
    // سنستخدم snake_case في قاعدة البيانات و camelCase في Java
    // ============================================================

    @NotBlank(message = "Full Name should not match username")
    @Size(max = 100, message = "Full Name Should not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    // ============================================================
    // bio: نبذة عن المستخدم - اختيارية
    // length = 500: حد أقصى 500 حرف
    // ============================================================
    @Column(length = 500)
    @Size(max = 500, message = "bio should be in 500 char")
    private String bio;

    // ============================================================
    // createdAt: تاريخ ووقت إنشاء الحساب
    // updatable = false: لا يمكن تحديث هذا الحقل بعد إنشائه
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    public enum Role {
        USER, ADMIN, SUPER_ADMIN
    }

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ============================================================
    // @PrePersist: JPA lifecycle callback
    // هذا الـ method يُنفذ تلقائياً قبل حفظ الكيان لأول مرة (INSERT)
    // نستخدمه لتعيين تاريخ الإنشاء والتحديث تلقائياً
    // ============================================================
    @PrePersist
    protected void onCreate() {
        // LocalDateTime.now(): يحصل على الوقت والتاريخ الحالي
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();  // نفس الوقت في البداية
    }

    // ============================================================
    // updatedAt: تاريخ ووقت آخر تحديث للحساب
    // ============================================================
    @Column()
    private LocalDateTime updatedAt;

    // ============================================================
    // @PreUpdate: JPA lifecycle callback
    // هذا الـ method يُنفذ تلقائياً قبل تحديث الكيان (UPDATE)
    // نستخدمه لتحديث تاريخ التعديل تلقائياً
    // ============================================================
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();  // تحديث الوقت الحالي
    }

    // ============================================================
    // ✅ Avatar: الصورة الرمزية المرفوعة من المستخدم
    // نحفظ مسار الملف فقط - الملف نفسه على السيرفر
    // مثال: /uploads/avatars/user_1_20260115.jpg
    // ============================================================
    @Column(name = "avatar_path", length = 500)
    private String avatarPath;

    // ============================================================
    // OAuth2 - من أين سجّل المستخدم؟ Google أم GitHub أم عادي؟
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    public enum AuthProvider {
        LOCAL, GOOGLE, GITHUB
    }

    // معرف المستخدم عند Google أو GitHub
    @Column(name = "provider_id", length = 100)
    private String providerId;

    // رابط الصورة من Google أو GitHub
    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
