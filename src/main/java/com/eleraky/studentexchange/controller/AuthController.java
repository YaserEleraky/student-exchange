package com.eleraky.studentexchange.controller;

// ============================================================
// DTOs الخاصة بنا
// ============================================================
import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.LoginRequest;
import com.eleraky.studentexchange.dto.response.JwtResponse;
import com.eleraky.studentexchange.dto.response.UserResponse;

// ============================================================
// الموديل والأمان
// ============================================================
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.security.JwtTokenProvider;
import com.eleraky.studentexchange.security.UserPrincipal;
import com.eleraky.studentexchange.service.UserService;

// ============================================================
// Validation
// ============================================================
import jakarta.validation.Valid;

// ============================================================
// Spring
// ============================================================
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// ============================================================
// Spring Security
// ============================================================
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

/**
 * AuthController: مسؤول عن المصادقة (Authentication)
 *
 * يوفر:
 * - POST /api/auth/register: تسجيل مستخدم جديد
 * - POST /api/auth/login: تسجيل الدخول والحصول على JWT
 *
 * هذه النقاط هي الوحيدة المفتوحة للجميع (بدون توكن)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * POST /api/auth/register
     * تسجيل مستخدم جديد
     *
     * @param request بيانات التسجيل
     * @return بيانات المستخدم الجديد (بدون كلمة المرور)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse userResponse = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * POST /api/auth/login
     * تسجيل الدخول والحصول على JWT Token
     *
     * سير العمل:
     * 1. نستقبل username و password
     * 2. نتحقق من صحتهما عبر UserService.authenticateUser()
     * 3. إذا كانت صحيحة:
     *    - ننشئ UserPrincipal
     *    - ننشئ Authentication object
     *    - ننشئ JWT Token
     *    - نرجع التوكن مع معلومات المستخدم
     *
     * @param request بيانات تسجيل الدخول
     * @return JWT Token ومعلومات المستخدم
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // ============================================================
        // الخطوة 1: التحقق من بيانات المستخدم
        // ============================================================
        User user = userService.authenticateUser(
                request.getUsername(),
                request.getPassword());

        // ============================================================
        // الخطوة 2: إنشاء UserPrincipal
        // ============================================================
        UserPrincipal userPrincipal = new UserPrincipal(user);

        // ============================================================
        // الخطوة 3: إنشاء Authentication Object
        //
        // UsernamePasswordAuthenticationToken:
        // - Principal: UserPrincipal
        // - Credentials: null (لأننا تحققنا بالفعل)
        // - Authorities: صلاحيات المستخدم
        // ============================================================
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities());

        // ============================================================
        // الخطوة 4: تعيين Authentication في SecurityContext
        //
        // لماذا هذا ضروري؟
        // لأن generateToken() يحتاج Authentication في SecurityContext
        // ============================================================
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ============================================================
        // الخطوة 5: إنشاء JWT Token
        // ============================================================
        String jwt = tokenProvider.generateToken(authentication);

        // ============================================================
        // الخطوة 6: إنشاء الاستجابة
        // ============================================================
        JwtResponse response = new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName());

        return ResponseEntity.ok(response);
    }
}
