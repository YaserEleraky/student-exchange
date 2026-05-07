package com.eleraky.studentexchange.repository;

import com.eleraky.studentexchange.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    // ============================================================
    // JpaRepository<Skill, Long>
    // Skill: الكيان
    // Long: نوع الـ ID
    // ============================================================

    // ============================================================
    // البحث عن كل مهارات مستخدم معين (بواسطة user id)
    // Spring سينشئ SQL: SELECT * FROM skills WHERE user_id = ?
    // ============================================================

    List<Skill> findByUserId(Long userId);

    // ============================================================
    // البحث عن مهارات حسب التصنيف
    // ============================================================
    List<Skill> findByCategory(String category);

    // ============================================================
    // البحث عن مهارات حسب النوع (تعليم أو تعلم)
    // ============================================================
    List<Skill> findBySkillType(Skill.SkillType skillType);

    // ============================================================
    // البحث عن مهارات حسب المستوى
    // ============================================================
    List<Skill> findBySkillLevel(Skill.SkillLevel skillLevel);

    // ============================================================
    // البحث عن مهارات بالاسم (بحث جزئي - غير حساس لحالة الأحرف)
    // Containing = LIKE %name%
    // IgnoreCase = لا يفرق بين كبير وصغير (Java = java)
    // ============================================================
    List<Skill> findByNameContainingIgnoreCase(String name);

    // ============================================================
    // البحث عن مهارات حسب التصنيف والنوع
    // مثال: كل مهارات "برمجة" من نوع "تعليم"
    // ============================================================
    List<Skill> findByCategoryAndSkillType(String category, Skill.SkillType skillType);

    // ============================================================
    // عد عدد مهارات مستخدم معين
    // ============================================================
    long countByUserId(Long userId);

}
