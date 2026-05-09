package com.eleraky.studentexchange.service;

// ============================================================
// نستورد الكيانات والـ DTOs التي نحتاجها
// ============================================================
import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.UpdateUserRequest;
import com.eleraky.studentexchange.dto.response.UserResponse;
import com.eleraky.studentexchange.model.User;

import java.util.List;
import java.util.Optional;

// ============================================================
// هذه واجهة (Interface) - وليس فيها تنفيذ
// لماذا نستخدم Interface؟
// 1. فصل التعريف عن التنفيذ (Separation of Concerns)
// 2. تسهيل اختبار الكود (Testing) باستخدام Mock
// 3. يمكن تغيير التنفيذ دون تغيير باقي الكود
// ============================================================

public interface  UserService {
    // ============================================================
    // إنشاء مستخدم جديد
    // CreateUserRequest: يحتوي على البيانات المطلوبة للإنشاء
    // UserResponse: نرجع بيانات المستخدم بعد الإنشاء (بدون كلمة المرور)
    // ============================================================

    UserResponse createUser(CreateUserRequest createUserRequest);
    /**
     * تسجيل مستخدم جديد مع تشفير كلمة المرور
     */
    UserResponse registerUser(CreateUserRequest request);

    /**
     * التحقق من بيانات تسجيل الدخول
     * ترجع المستخدم إذا كانت البيانات صحيحة
     */
    User authenticateUser(String usernameOrEmail, String password);
    // ============================================================
    // الحصول على جميع المستخدمين
    // List<UserResponse>: قائمة بكل المستخدمين
    // ============================================================
    List<UserResponse> getAllUsers();

    // ============================================================
    // البحث عن مستخدم بالـ ID
    // Optional: قد لا يوجد مستخدم بهذا الـ ID
    // ============================================================
    Optional<UserResponse> getUserById(Long id);

    // ============================================================
    // البحث عن مستخدم باسم المستخدم
    // ============================================================
    Optional<UserResponse> getUserByUsername(String username);

    // ============================================================
    // تحديث بيانات مستخدم
    // id: معرف المستخدم
    // request: البيانات الجديدة
    // ============================================================
    Optional<UserResponse> updateUser(Long id, UpdateUserRequest request);

    // ============================================================
    // حذف مستخدم
    // ============================================================
    void deleteUser(Long id);

    // ============================================================
    // التحقق من وجود مستخدم
    // ============================================================
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
