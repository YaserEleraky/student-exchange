package com.eleraky.studentexchange.security;
// ============================================================
// استيراد خدمات الأمان الخاصة بنا
// ============================================================
import com.eleraky.studentexchange.security.CustomUserDetailsService;
import com.eleraky.studentexchange.security.JwtAuthenticationFilter;

// ============================================================
// Spring Security:
// @EnableWebSecurity: تفعيل Spring Security
// @Configuration: تخبر Spring أن هذه الفئة تحتوي على @Beans
// @Bean: طريقة تنتج Bean - كائن يديره Spring
// ============================================================
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// ============================================================
// إعدادات الأمان التفصيلية
// ============================================================
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * SecurityConfig: إعدادات الأمان الرئيسية للتطبيق
 *
 * هذه الفئة هي "عقل" نظام الأمان
 * تحدد:
 * 1. كيفية تشفير كلمات المرور
 * 2. أي المسارات محمية وأيها مفتوحة
 * 3. كيفية المصادقة (JWT في حالتنا)
 *
 * @Configuration + @EnableWebSecurity:
 * - @Configuration: فئة إعدادات (يمكن أن تحتوي على @Beans)
 * - @EnableWebSecurity: تفعيل Spring Security وتخصيصه
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * PasswordEncoder: لتشفير كلمات المرور
     *
     * BCryptPasswordEncoder:
     * - أقوى خوارزمية تشفير لكلمات المرور
     * - بطيئة عمداً (لمنع هجمات Brute Force)
     * - تستخدم Salt عشوائي لكل كلمة مرور (حتى لو كانت الكلمتين متطابقتين، التشفير مختلف)
     * - لا يمكن فك التشفير! (One-way hash)
     *
     * كيف تعمل؟
     * - encode("123456"): يرجع "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * - matches("123456", hash): يقارن كلمة المرور مع الـ hash
     *
     * @Bean: تجعل هذه الدالة تنتج Bean (كائن يديره Spring)
     * Bean: كائن واحد Shared في كل التطبيق (Singleton)
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        // ============================================================
        // BCryptPasswordEncoder():
        // - strength: 10 (الافتراضي) - كلما زاد الرقم، كان التشفير أقوى وأبطأ
        // - 10 يعني 2^10 = 1024 دورة تشفير
        // ============================================================
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager: مدير المصادقة
     *
     * هو المسؤول عن عملية المصادقة
     * يستخدم UserDetailsService لتحميل المستخدم
     * ويستخدم PasswordEncoder للتحقق من كلمة المرور
     *
     * @param http HttpSecurity
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // ============================================================
        // AuthenticationManagerBuilder: لبناء AuthenticationManager
        // userDetailsService(): نحدد خدمة تحميل المستخدمين
        // passwordEncoder(): نحدد طريقة تشفير كلمة المرور
        // ============================================================
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * SecurityFilterChain: قلب إعدادات الأمان
     *
     * هذا هو أهم @Bean في الأمان!
     * نحدد هنا كل قواعد الأمان
     *
     * @param http HttpSecurity - الكائن الذي نبني عليه إعدادات الأمان
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ============================================================
                // CSRF (Cross-Site Request Forgery):
                // هجوم حيث موقع خبيث يجبر متصفحك على إرسال طلب لموقع آخر
                //
                // لماذا نعطله؟
                // 1. JWT يحمي من CSRF تلقائياً (لا نستخدم Cookies)
                // 2. CSRF مهم فقط مع Server-Side Rendering
                // 3. REST APIs مع JWT لا تحتاج CSRF
                //
                // csrf.disable(): تعطيل حماية CSRF
                // في Spring Security 6.x، الصيغة تغيرت إلى Lambda DSL
                // ============================================================
                .csrf(csrf -> csrf.disable())

                // ============================================================
                // Session Management:
                //
                // لماذا STATELESS؟
                // 1. لا نستخدم جلسات HTTP (HttpSession)
                // 2. كل طلب مستقل ويحمل JWT
                // 3. أداء أفضل (لا تخزين للجلسات على السيرفر)
                // 4. مناسب لـ Microservices و Horizontal Scaling
                //
                // SessionCreationPolicy.STATELESS:
                // Spring Security لن ينشئ أو يستخدم HttpSession أبداً
                // ============================================================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ============================================================
                // Authorization Rules:
                // نحدد من يمكنه الوصول لأي مسار
                //
                // الترتيب مهم! القواعد تطبق من الأعلى إلى الأسفل
                // أول قاعدة تطابق تُستخدم
                // ============================================================
                .authorizeHttpRequests(auth -> auth
                        // ============================================================
                        // permitAll(): السماح للجميع (مع أو بدون توكن)
                        //
                        // requestMatchers(): يطابق المسارات
                        // ** = أي شيء بعد هذا المسار
                        // /api/auth/** = كل ما يبدأ بـ /api/auth/
                        // ============================================================

                        // نقاط نهاية المصادقة (التسجيل والدخول)
                        .requestMatchers("/", "/api/auth/**").permitAll()

                        // البحث عن المهارات - متاح للجميع
                        .requestMatchers("/api/skills/search/**").permitAll()
                        .requestMatchers("/api/skills/category/**").permitAll()
                        .requestMatchers("/api/skills/type/**").permitAll()

                        // أي مسار عام آخر
                        .requestMatchers("/api/public/**").permitAll()

                        // ============================================================
                        // anyRequest().authenticated():
                        // أي طلب آخر (غير المذكور أعلاه) يجب أن يكون مصادقاً عليه
                        // يجب إرسال JWT Token صالح
                        // ============================================================
                        .anyRequest().authenticated()
                )

                // ============================================================
                // إضافة فلتر JWT قبل UsernamePasswordAuthenticationFilter
                //
                // UsernamePasswordAuthenticationFilter:
                // الفلتر الافتراضي الذي يعالج POST /login
                //
                // لماذا قبله؟
                // لأننا نريد معالجة JWT أولاً
                // إذا وجدنا JWT صالح، نضبط المستخدم
                // وإلا، نترك Spring Security يستخدم المصادقة الافتراضية
                // ============================================================
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        // ============================================================
        // http.build(): بناء SecurityFilterChain
        // ============================================================
        return http.build();
    }
}
