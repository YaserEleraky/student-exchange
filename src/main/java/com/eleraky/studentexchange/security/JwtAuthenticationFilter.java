package com.eleraky.studentexchange.security;

// ============================================================
// Jakarta Servlet API: للتعامل مع HTTP requests
// HttpServletRequest: الطلب القادم من العميل
// HttpServletResponse: الاستجابة التي سنرسلها
// FilterChain: سلسلة الفلاتر
// مصدرها: Jakarta EE
// ============================================================
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// ============================================================
// Spring Security:
// UsernamePasswordAuthenticationToken: تمثيل لعملية المصادقة
// SecurityContextHolder: يحمل SecurityContext الحالي
// WebAuthenticationDetailsSource: لبناء تفاصيل المصادقة من HTTP request
// ============================================================
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

// ============================================================
// Spring:
// @Component: تجعل الفئة Bean
// @Autowired: حقن التبعيات
// ============================================================
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// ============================================================
// StringUtils: أدوات للتعامل مع النصوص
// OncePerRequestFilter: فلتر يضمن التنفيذ مرة واحدة لكل طلب
// ============================================================
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter: فلتر المصادقة باستخدام JWT
 *
 * هذا الفلتر هو قلب نظام الأمان!
 * يعمل كـ "حارس بوابة" - يفحص كل طلب قبل وصوله إلى Controllers
 *
 * سير العمل:
 * 1. يستخرج JWT من Authorization Header
 * 2. يتحقق من صحة التوكن
 * 3. إذا كان التوكن صالحاً:
 *    - يحمل بيانات المستخدم من قاعدة البيانات
 *    - يضبط المستخدم في SecurityContext
 * 4. إذا كان التوكن غير صالح: المستخدم يبقى "غير معروف" (Anonymous)
 *
 * OncePerRequestFilter:
 * - فلتر من Spring يضمن تنفيذه مرة واحدة فقط لكل طلب HTTP
 * - حتى لو كان هناك forward أو include، لا يتكرر
 * - مصدرها: spring.io/spring-web
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    /**
     * doFilterInternal: نقطة الدخول الرئيسية للفلتر
     * تستدعى لكل طلب HTTP
     *
     * @param request الطلب HTTP
     * @param response الاستجابة HTTP
     * @param filterChain سلسلة الفلاتر
     */

    /**
     * getTokenFromRequest: استخراج JWT من Authorization Header
     *
     * صيغة Authorization Header:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.rTCH8cLoGxAm...
     *
     * "Bearer " متبوعة بالتوكن
     *
     * @param request الطلب HTTP
     * @return JWT Token أو null إذا لم يوجد
     */

    private String getTokenFromRequest(HttpServletRequest request){
        // ============================================================
        // getHeader("Authorization"): يجلب قيمة Authorization Header
        // ============================================================
        String bearerToken = request.getHeader("Authorization");
        // ============================================================
        // التحقق من أن الـ Header موجود ويبدأ بـ "Bearer "
        // substring(7): نتخطى "Bearer " (7 أحرف: B,e,a,r,e,r, ,)
        // ============================================================
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            SecurityContextHolder.clearContext();
            // ============================================================
            // الخطوة 1: استخراج JWT من الـ Header
            // ============================================================
            String token = getTokenFromRequest(request);
            // ============================================================
            // الخطوة 2: التحقق من صحة التوكن
            // StringUtils.hasText(): هل النص موجود وليس فارغاً؟
            // ============================================================
            if(StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                // ============================================================
                // الخطوة 3: استخراج ID المستخدم من التوكن
                // ============================================================
                Long userId = tokenProvider.getUserIdFromToken(token);

                // ============================================================
                // الخطوة 4: تحميل بيانات المستخدم كاملة من قاعدة البيانات
                // لماذا نحمله من قاعدة البيانات؟ لماذا لا نستخدم البيانات في JWT؟
                // 1. البيانات في JWT قد تكون قديمة (تغيرت صلاحيات المستخدم مثلاً)
                // 2. نحتاج الأدوار والصلاحيات من قاعدة البيانات
                // 3. التحقق من أن المستخدم لا يزال موجوداً (لم يُحذف)
                // ============================================================
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                // ============================================================
                // الخطوة 5: إنشاء Authentication Object
                //
                // UsernamePasswordAuthenticationToken:
                // - النوع الأساسي للمصادقة في Spring Security
                // - المعامل الأول: Principal (المستخدم)
                // - المعامل الثاني: Credentials (كلمة المرور - null لأننا استخدمنا JWT)
                // - المعامل الثالث: Authorities (الصلاحيات)
                // ============================================================
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,  // لا نحتاج كلمة المرور هنا
                                userDetails.getAuthorities());

                // ============================================================
                // إضافة تفاصيل إضافية (IP, Session ID, إلخ)
                // ============================================================
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // طباعة تفاصيل المصادقة إلى وحدة التحكم
                System.out.println("*************************************************** " +
                        "Authentication details: *****************************************************" +
                        "" + authentication.toString());
                // ============================================================
                // الخطوة 6: تعيين المستخدم في SecurityContext
                //
                // SecurityContextHolder:
                // - يحمل SecurityContext للـ Thread الحالي
                // - بعد هذا السطر، Spring Security يعرف من هو المستخدم
                // - يمكننا استخدام SecurityContextHolder.getContext().getAuthentication()
                //   في أي مكان في الكود للحصول على المستخدم الحالي
                // ============================================================
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception e) {
            System.out.println("*************************************************** " +
                    "Error while filtering: *****************************************************" +
                    "" + e.getMessage());
            e.printStackTrace();

            }


        // ============================================================
        // الخطوة 7: متابعة سلسلة الفلاتر
        //
        // filterChain.doFilter(): يمرر الطلب إلى الفلتر التالي
        // إذا لم نستدعِ هذه الدالة، الطلب لن يصل إلى Controller أبداً!
        // ============================================================
        filterChain.doFilter(request, response);
    }
}
