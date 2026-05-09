package com.eleraky.studentexchange.security;

// ============================================================
// الموديل والـ Repository الخاصين بنا
// ============================================================
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.UserRepository;

// ============================================================
// Spring Security:
// UserDetails: الواجهة التي تمثل المستخدم
// UserDetailsService: الواجهة التي تحمل المستخدم من قاعدة البيانات
// UsernameNotFoundException: استثناء عندما لا يوجد المستخدم
// مصدرها: spring.io/spring-security
// ============================================================
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// ============================================================
// @Service: تخبر Spring أن هذه الفئة هي Service Bean
// @Autowired: حقن التبعيات تلقائياً
// @Transactional: إدارة المعاملات مع قاعدة البيانات
// ============================================================
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService: خدمة تحميل بيانات المستخدم
 *
 * UserDetailsService هي واجهة من Spring Security
 * لها دالة واحدة فقط: loadUserByUsername()
 *
 * لماذا نحتاج هذه الخدمة؟
 * 1. Spring Security لا يعرف كيف يبحث عن المستخدمين في قاعدة البيانات
 * 2. هذه الخدمة هي "الجسر" بين Spring Security وقاعدة البيانات
 * 3. عندما يحاول مستخدم تسجيل الدخول، Spring Security يستدعي loadUserByUsername()
 *
 * سير العمل:
 * 1. المستخدم يرسل username + password
 * 2. Spring Security يستدعي loadUserByUsername(username)
 * 3. نبحث عن المستخدم في قاعدة البيانات
 * 4. نرجع UserDetails (UserPrincipal)
 * 5. Spring Security يقارن كلمة المرور تلقائياً
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // ============================================================
        // البحث عن المستخدم:
        // 1. أولاً نبحث بـ username
        // 2. إذا لم نجده، نبحث بـ email
        // 3. إذا لم نجده أيضاً، نرمي استثناء
        //
        // orElseGet(): تنفذ الـ lambda فقط إذا كانت Optional فارغة
        // هذا أفضل من orElse() لأننا لا ننشئ الكائن إلا عند الحاجة
        // ============================================================
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "المستخدم غير موجود باسم: " + usernameOrEmail)));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "المستخدم غير موجود بالـ ID: " + id));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
