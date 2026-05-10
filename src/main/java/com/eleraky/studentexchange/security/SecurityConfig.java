package com.eleraky.studentexchange.security;

import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.UserRepository;
import com.eleraky.studentexchange.service.impl.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // IF_REQUIRED: بدون session للـ API، لكن يسمح بـ session مؤقتة لـ OAuth2 flow
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth
                // OAuth2 routes - يجب أن تكون مفتوحة
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/", "/api/auth/**").permitAll()
                .requestMatchers("/uploads/**").authenticated()
                .requestMatchers("/api/skills/search/**").permitAll()
                .requestMatchers("/api/skills/category/**").permitAll()
                .requestMatchers("/api/skills/type/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/superadmin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/users", "/api/users/**").permitAll()
                .requestMatchers("/api/skills/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )

            // ============================================================
            // OAuth2 Login
            // ============================================================
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService())
                )

                // نجاح تسجيل الدخول → نولّد JWT ونعمل redirect
                .successHandler((request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    Long   userId   = (Long)   oAuth2User.getAttributes().get("userId");

                    // اسم الـ provider: "google" أو "github"
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                    String provider = oauthToken.getAuthorizedClientRegistrationId();

                    User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                    String jwt = jwtTokenProvider.generateToken(user);

                    // redirect إلى endpoint يعرض رسالة الترحيب مع الـ token
                    response.sendRedirect("/api/auth/oauth2/success?token=" + jwt + "&provider=" + provider);
                })

                // فشل تسجيل الدخول → نرجع خطأ واضح
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"error\":\"OAuth2 login failed: " + exception.getMessage() + "\"}"
                    );
                })
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // OAuth2 User Service
    // يستقبل بيانات المستخدم من Google أو GitHub
    // ويبحث عنه في قاعدة البيانات أو ينشئه لأول مرة
    // ============================================================
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return userRequest -> {
            // 1. جلب بيانات المستخدم من Google أو GitHub
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // 2. استخراج البيانات - كل provider له مفاتيح مختلفة
            String email, name, providerId, picture;

            if ("github".equals(registrationId)) {
                // GitHub attributes
                providerId = String.valueOf(attributes.get("id"));
                name       = (String) attributes.getOrDefault("name", attributes.get("login"));
                email      = (String) attributes.get("email");
                picture    = (String) attributes.get("avatar_url");
                // بعض حسابات GitHub تُخفي الإيميل
                if (email == null || email.isBlank()) {
                    email = attributes.get("login") + "@github.noemail";
                }
            } else {
                // Google attributes
                providerId = (String) attributes.get("sub");
                name       = (String) attributes.get("name");
                email      = (String) attributes.get("email");
                picture    = (String) attributes.get("picture");
            }

            User.AuthProvider authProvider = "github".equals(registrationId)
                ? User.AuthProvider.GITHUB
                : User.AuthProvider.GOOGLE;

            final String fEmail    = email;
            final String fName     = name != null ? name : email;
            final String fId       = providerId;
            final String fPicture  = picture;

            // 3. البحث عن المستخدم في DB أو إنشاؤه لأول مرة
            User user = userRepository.findByEmail(fEmail).orElseGet(() -> {
                String base     = fEmail.substring(0, fEmail.indexOf('@'));
                String username = userRepository.existsByUsername(base)
                    ? base + "_" + fId.substring(0, Math.min(6, fId.length()))
                    : base;

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(fEmail);
                newUser.setFullName(fName);
                newUser.setPassword("");
                newUser.setProvider(authProvider);
                newUser.setProviderId(fId);
                newUser.setImageUrl(fPicture);
                newUser.setRole(User.Role.USER);

                User savedUser = userRepository.save(newUser);

                // تحميل الصورة من Google/GitHub وحفظها محلياً
                if (fPicture != null) {
                    try {
                        String localPath = fileStorageService.saveAvatarFromUrl(fPicture, savedUser.getId());
                        savedUser.setAvatarPath(localPath);
                        return userRepository.save(savedUser);
                    } catch (Exception e) {
                        System.out.println("Avatar download failed: " + e.getMessage());
                    }
                }
                return savedUser;
            });

            // للمستخدمين الموجودين الذين ليس لديهم صورة محلية بعد — حمّلها الآن
            if (user.getAvatarPath() == null && fPicture != null) {
                try {
                    String localPath = fileStorageService.saveAvatarFromUrl(fPicture, user.getId());
                    user.setAvatarPath(localPath);
                    userRepository.save(user);
                } catch (Exception e) {
                    System.out.println("Avatar download failed for existing user: " + e.getMessage());
                }
            }

            // 4. إضافة userId و username للـ attributes حتى نستخدمهم في successHandler
            Map<String, Object> enriched = new HashMap<>(attributes);
            enriched.put("userId",     user.getId());
            enriched.put("dbUsername", user.getUsername());

            // nameAttributeKey: المفتاح الرئيسي المختلف بين Google و GitHub
            String nameAttrKey = "github".equals(registrationId) ? "id" : "sub";

            return new DefaultOAuth2User(oAuth2User.getAuthorities(), enriched, nameAttrKey);
        };
    }
}
