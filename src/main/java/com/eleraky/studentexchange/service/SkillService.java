package com.eleraky.studentexchange.service;


import com.eleraky.studentexchange.dto.request.SkillRequest;
import com.eleraky.studentexchange.dto.response.SkillResponse;

import java.util.List;
// تأكد من إضافة هذه الـ imports في أعلى الملف:
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
public interface SkillService {

    /**
     * إضافة مهارة جديدة لمستخدم
     * @param userId معرف المستخدم
     * @param request بيانات المهارة
     * @return المهارة المضافة
     */
    SkillResponse addSkill(Long userId, SkillRequest request);


    /**
     * الحصول على جميع المهارات
     * @return قائمة بكل المهارات
     */
    List<SkillResponse> getAllSkills();

    /**
     * الحصول على مهارة بالـ ID
     * @param id معرف المهارة
     * @return المهارة إذا وجدت
     */
    SkillResponse getSkillById(Long id);

    /**
     * الحصول على كل مهارات مستخدم معين
     * @param userId معرف المستخدم
     * @return قائمة مهارات المستخدم
     */
    List<SkillResponse> getSkillsByUser(Long userId);

    /**
     * البحث عن مهارات بالاسم
     * @param name اسم المهارة (جزئي)
     * @return قائمة المهارات المطابقة
     */
    List<SkillResponse> searchSkills(String name);

    /**
     * الحصول على مهارات حسب التصنيف
     * @param category التصنيف
     * @return قائمة المهارات
     */
    List<SkillResponse> getSkillsByCategory(String category);


    /**
     * الحصول على مهارات حسب النوع (تعليم/تعلم)
     * @param skillType نوع المهارة
     * @return قائمة المهارات
     */
    List<SkillResponse> getSkillsByType(String skillType);


    /**
     * تحديث مهارة
     * @param id معرف المهارة
     * @param request البيانات الجديدة
     * @return المهارة بعد التحديث
     */
    SkillResponse updateSkill(Long id, SkillRequest request);

    /**
     * حذف مهارة
     * @param id معرف المهارة
     */
    void deleteSkill(Long id);

    /**
     * تحديث مهارة معينة لمستخدم معين (مع التحقق من الملكية)
     * @param userId معرف المستخدم
     * @param skillId معرف المهارة
     * @param request البيانات الجديدة
     * @return المهارة بعد التحديث
     */
    SkillResponse updateSkillForUser(Long userId, Long skillId, SkillRequest request);

    /**
     * حذف مهارة معينة لمستخدم معين (مع التحقق من الملكية)
     * @param userId معرف المستخدم
     * @param skillId معرف المهارة
     */
    void deleteSkillForUser(Long userId, Long skillId);

    /**
     * الحصول على إحصائيات مهارات المستخدم
     * @param userId معرف المستخدم
     * @return خريطة بالإحصائيات
     */
    Map<String, Object> getUserSkillStats(Long userId);
}
