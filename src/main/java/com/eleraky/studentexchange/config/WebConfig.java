package com.eleraky.studentexchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig: إعدادات الويب
 *
 * الوظيفة: السماح بعرض الملفات من فولدر uploads/
 *
 * بدون هذا الملف:
 * - /uploads/avatars/photo.jpg → 404 أو 403
 *
 * مع هذا الملف:
 * - /uploads/avatars/photo.jpg → الصورة تظهر ✅
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * addResourceHandlers: إضافة معالج للموارد الثابتة
     *
     * /uploads/** = أي طلب يبدأ بـ /uploads/
     * file:uploads/ = ابحث في فولدر uploads الموجود في جذر المشروع
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                // أي URL يبدأ بـ /uploads/
                .addResourceHandler("/uploads/**")
                // ابحث في فولدر uploads/ على السيرفر
                .addResourceLocations("file:uploads/");
    }
}