package com.eleraky.studentexchange.security;

// ============================================================
// استيراد الكيان User من الموديل الخاص بنا
// ============================================================
import com.eleraky.studentexchange.model.User;

// ============================================================
// Spring Security Core:
// GrantedAuthority: يمثل صلاحية أو دور (مثل ROLE_USER, ROLE_ADMIN)
// SimpleGrantedAuthority: تنفيذ بسيط لـ GrantedAuthority
// UserDetails: الواجهة الأساسية لتمثيل المستخدم في Spring Security
// مصدرها: spring.io/spring-security
// ============================================================
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * UserPrincipal: يغلف (يُغلّف) كائن User ليكون متوافقاً مع Spring Security
 *
 * لماذا نحتاج هذا؟
 * 1. Spring Security لا يفهم كائن User الخاص بنا مباشرة
 * 2. Spring Security يحتاج كائن يطبق واجهة UserDetails
 * 3. هذا الكائن هو "جسر" بين الموديل الخاص بنا و Spring Security
 *
 * هذا المفهوم يسمى: Adapter Pattern (نمط المحول)
 *
 * UserDetails هي واجهة (Interface) من Spring Security
 * أي فئة تطبقها تصبح قادرة على تمثيل مستخدم في نظام الأمان
 */
public class UserPrincipal implements UserDetails {

    // ============================================================
    // SerialVersionUID: للـ Serialization
    // مهم لأن UserDetails قابلة للتسلسل (Serializable)
    // ============================================================
    private static final long serialVersionUID = 1L;

    // معلومات المستخدم
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    // ============================================================
    // Collection<? extends GrantedAuthority>: مجموعة الصلاحيات
    // GrantedAuthority: واجهة تمثل "صلاحية" أو "دور"
    // SimpleGrantedAuthority: "ROLE_USER", "ROLE_ADMIN", إلخ
    // ============================================================
    private Collection<? extends GrantedAuthority> authorities;
    // ============================================================
    // Getters خاصة بـ UserPrincipal
    // هذه ليست من UserDetails - أضفناها للاستخدام في الكود
    // ============================================================

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Constructor: يحول User (الموديل الخاص بنا) إلى UserPrincipal
     *
     * @param user كائن المستخدم من قاعدة البيانات
     */
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();

        // ============================================================
        // كلمة المرور المشفرة (BCrypt)
        // نحملها كما هي - لا نفك التشفير (ولا يمكن فكه أصلاً!)
        // Spring Security سيقارنها تلقائياً عند الحاجة
        // ============================================================
        this.password = user.getPassword();
        this.fullName = user.getFullName();

        // ============================================================
        // تعيين الصلاحيات (الأدوار)
        // حالياً: كل المستخدمين لهم دور "ROLE_USER"
        //
        // Collections.singletonList(): تنشئ List تحتوي على عنصر واحد فقط
        // لماذا List وليس عنصر واحد؟
        // لأن المستخدم يمكن أن يكون له عدة أدوار:
        // مثلاً: ROLE_USER, ROLE_TEACHER, ROLE_ADMIN
        //
        // لاحقاً سنضيف جدول الأدوار ونحملها من قاعدة البيانات
        // ============================================================
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    // ============================================================
    // تطبيق دوال واجهة UserDetails
    // هذه الدوال يجب تنفيذها لأننا implements UserDetails
    // ============================================================

    /**
     * getAuthorities(): ترجع صلاحيات المستخدم
     * Spring Security يستخدمها لتحديد: هل هذا المستخدم مسموح له بتنفيذ هذا الإجراء؟
     * مثال: @PreAuthorize("hasRole('ADMIN')") - يتحقق من وجود ROLE_ADMIN
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * getPassword(): ترجع كلمة المرور (المشفرة)
     * Spring Security يستخدمها للمقارنة عند تسجيل الدخول
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * getUsername(): ترجع اسم المستخدم
     * Spring Security يستخدمه كمعرف أساسي للمستخدم
     */
    @Override
    public String getUsername() {
        return username;
    }

    // ============================================================
    // دوال حالة الحساب
    // كلها ترجع true حالياً (الحساب سليم)
    // في نظام حقيقي، هذه القيم تأتي من قاعدة البيانات
    // ============================================================

    /**
     * isAccountNonExpired(): هل الحساب غير منتهي الصلاحية؟
     * مثال: اشتراكات تنتهي بعد فترة
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * isAccountNonLocked(): هل الحساب غير مقفل؟
     * مثال: قفل الحساب بعد 3 محاولات فاشلة
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * isCredentialsNonExpired(): هل بيانات الاعتماد غير منتهية؟
     * مثال: كلمة المرور يجب تغييرها كل 90 يوم
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * isEnabled(): هل الحساب مفعل؟
     * مثال: تفعيل الحساب عبر البريد الإلكتروني
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
