package com.eleraky.studentexchange.security;

// ============================================================
// JJWT: مكتبة JSON Web Token لـ Java
// Jwts: الفئة الرئيسية لبناء وتحليل JWT
// Claims: البيانات المخزنة داخل JWT (Payload)
// SignatureAlgorithm: خوارزمية التوقيع (HS256, RS256, إلخ)
// Keys: لإنشاء مفاتيح التوقيع
// مصدرها: github.com/jwtk/jjwt
// ============================================================
import com.eleraky.studentexchange.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

// ============================================================
// Spring:
// @Value: لقراءة القيم من application.yml أو properties
// @Component: تخبر Spring أن هذه الفئة Bean
// Authentication: يمثل عملية المصادقة في Spring Security
// ============================================================
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// ============================================================
// Java Standard:
// SecretKey: مفتاح سري للتشفير (من javax.crypto)
// StandardCharsets: ترميز UTF-8
// Date: للتعامل مع التواريخ
// ============================================================
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
/**
 * JwtTokenProvider: مسؤول عن كل ما يتعلق بـ JWT (JSON Web Token)
 *
 * المهام:
 * 1. إنشاء JWT Token (generateToken)
 * 2. التحقق من صحة JWT Token (validateToken)
 * 3. استخراج المعلومات من JWT Token (getUserIdFromToken)
 *
 * ما هو JWT؟
 * JWT (JSON Web Token) هو معيار RFC 7519
 * هو "تذكرة دخول رقمية" تثبت هوية المستخدم
 *
 * شكل JWT:
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.rTCH8cLoGxAm_xw68z-zXVKi9ie6xJn9tnVWjd_9ftE
 *
 * الأجزاء الثلاثة:
 * 1. Header (الأحمر): {"alg":"HS256","typ":"JWT"} - معلومات عن الخوارزمية والنوع
 * 2. Payload (الأزرق): {"sub":"1","username":"yasser"} - البيانات
 * 3. Signature (الأخضر): التوقيع الرقمي - يثبت أن البيانات لم يتم التلاعب بها
 *
 * الميزات:
 * - Stateless: السيرفر لا يحتاج تخزين جلسات
 * - Self-contained: التوكن يحتوي على كل المعلومات
 * - Secure: لا يمكن تزويره بدون المفتاح السري
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:this-is-a-very-long-secret-key-for-jwt-token-generation-2024-minimum-256-bits}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    // ============================================================
    // @Value("${...}") : يقرأ القيمة من application.yml
    //
    // ${app.jwt.secret:default-value}
    // الجزء قبل : هو المفتاح في application.yml
    // الجزء بعد : هو القيمة الافتراضية إذا لم يوجد المفتاح
    //
    // لماذا نستخدم @Value؟
    // 1. فصل الإعدادات عن الكود
    // 2. تغيير القيم بدون إعادة ترجمة الكود
    // 3. قيم مختلفة لكل بيئة (dev, prod)
    //
    // تحذير أمني:
    // في الإنتاج، يجب أن يكون المفتاح السري:
    // 1. طويلاً (256 بت على الأقل)
    // 2. في متغير بيئة (وليس في الكود)
    // 3. مختلفاً لكل بيئة
    // ===========================================================

    /**
     * getSigningKey: إنشاء مفتاح التوقيع من النص السري
     *
     * HMAC-SHA256 (HS256) هي خوارزمية توقيع متناظرة
     * نفس المفتاح يستخدم للتوقيع والتحقق
     *
     * @return SecretKey مفتاح سري للتوقيع
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * generateToken: إنشاء JWT Token جديد
     *
     * @paramauthentication كائن المصادقة من Spring Security
     * @return JWT Token كنص
     *
     * سير العمل:
     * 1. نستخرج UserPrincipal من Authentication
     * 2. ننشئ تاريخ الإصدار والانتهاء
     * 3. نبني JWT باستخدام Builder Pattern
     * 4. نوقع التوكن بالمفتاح السري
     * 5. نرجع التوكن كنص
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // ============================================================
        // Jwts.builder(): بدء بناء JWT جديد
        //
        // .subject(): المعرف الأساسي (ID المستخدم)
        // Standard Claim: "sub" = Subject
        //
        // .claim(): إضافة بيانات مخصصة (Custom Claims)
        // هذه البيانات ستكون متاحة في Payload
        //
        // .issuedAt(): تاريخ الإصدار
        // Standard Claim: "iat" = Issued At
        //
        // .expiration(): تاريخ الانتهاء
        // Standard Claim: "exp" = Expiration
        //
        // .signWith(): توقيع التوكن بالمفتاح السري
        // هذا يمنع التلاعب بالتوكن
        //
        // .compact(): تحويل كل هذا إلى نص JWT النهائي
        // ============================================================
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("fullName", user.getFullName())
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    /**
     * getUserIdFromToken: استخراج ID المستخدم من JWT Token
     *
     * @param token JWT Token
     * @return ID المستخدم
     *
     * سير العمل:
     * 1. نحلل التوكن باستخدام المفتاح السري
     * 2. نستخرج الـ Claims (البيانات)
     * 3. نرجع Subject (الذي وضعنا فيه ID المستخدم)
     */
    public Long getUserIdFromToken(String token) {
        // ============================================================
        // Jwts.parser(): بدء تحليل JWT
        // .verifyWith(): التحقق من التوقيع باستخدام المفتاح السري
        // .build(): بناء الـ Parser
        // .parseSignedClaims(): تحليل التوكن واستخراج الـ Claims
        // .getPayload(): الحصول على البيانات (Payload)
        // ============================================================
        Claims claims = Jwts.parser().verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        // ============================================================
        // getSubject(): ترجع قيمة "sub" من JWT
        // التي وضعناها كـ ID المستخدم
        // ============================================================
        return Long.parseLong(claims.getSubject());
    }
    /**
     * validateToken: التحقق من صحة JWT Token
     *
     * @param token JWT Token
     * @return true إذا كان التوكن صالحاً، false إذا كان غير صالح
     *
     * يتحقق من:
     * 1. التوقيع صحيح (لم يتم التلاعب بالتوكن)
     * 2. التوكن لم ينتهِ صلاحيته
     * 3. صيغة التوكن صحيحة
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
