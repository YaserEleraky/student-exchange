package com.eleraky.studentexchange.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * FileStorageService: خدمة بسيطة لحفظ الملفات على السيرفر
 *
 * ============================================================
 * كيف تعمل؟
 * ============================================================
 * 1. نستقبل الملف من المستخدم (MultipartFile)
 * 2. ننشئ اسم فريد للملف (باستخدام UUID)
 * 3. نحفظ الملف في مجلد uploads/avatars/
 * 4. نرجع مسار الملف (لحفظه في قاعدة البيانات)
 */
@Service
public class FileStorageService {
    // ============================================================
    // @Value: قراءة قيمة من application.yml
    // ${app.upload.avatar-dir} = uploads/avatars
    // ============================================================
    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    /**
     * حفظ صورة رمزية
     *
     * @param file الملف المرفوع
     * @param userId معرف المستخدم (لتسمية الملف)
     * @return مسار الملف المحفوظ
     */
    public String saveAvatar(MultipartFile file, Long userId) throws IOException {
        // ============================================================
        // 1. التحقق من أن الملف صورة
        // ============================================================
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("الملف يجب أن يكون صورة (JPG, PNG, GIF)");
        }
        // ============================================================
        // 2. استخراج امتداد الملف
        // originalFilename: "myphoto.jpg"
        // getExtension: "jpg"
        // ============================================================
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // ============================================================
        // 3. إنشاء اسم فريد للملف
        // UUID: معرف فريد عالمياً (مثل: a1b2c3d4-e5f6-7890-abcd-ef1234567890)
        // نأخذ أول 8 أحرف فقط للتبسيط
        //
        // اسم الملف النهائي:
        // user_1_a1b2c3d4.jpg
        // ============================================================
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = "user_" + userId + "_" + uniqueId + extension;

        // ============================================================
        // 4. إنشاء المجلد إذا لم يكن موجوداً
        // Paths.get(): إنشاء مسار
        // Files.createDirectories(): إنشاء كل المجلدات في المسار
        // ============================================================
        Path uploadPath = Paths.get(avatarDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // ============================================================
        // 5. نسخ الملف إلى المجلد
        // resolve(): دمج المسار مع اسم الملف
        // StandardCopyOption.REPLACE_EXISTING: استبدال الملف إذا موجود
        //
        // المسار النهائي: uploads/avatars/user_1_a1b2c3d4.jpg
        // ============================================================
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // ============================================================
        // 6. إرجاع المسار النسبي (للوصول عبر URL)
        // ============================================================
        return "/" + avatarDir + "/" + newFilename;
    }
    /**
     * حذف صورة قديمة
     *
     * @param avatarPath مسار الصورة القديمة
     */
    public void deleteAvatar(String avatarPath) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            try {
                // إزالة "/" من البداية
                Path filePath = Paths.get(avatarPath.substring(1));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // الملف غير موجود أو لا يمكن حذفه - تجاهل
            }
        }
    }
}
