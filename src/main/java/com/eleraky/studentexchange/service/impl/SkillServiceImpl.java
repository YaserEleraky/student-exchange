package com.eleraky.studentexchange.service.impl;

import com.eleraky.studentexchange.dto.request.SkillRequest;
import com.eleraky.studentexchange.dto.response.SkillResponse;
import com.eleraky.studentexchange.model.Skill;
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.SkillRepository;
import com.eleraky.studentexchange.repository.UserRepository;
import com.eleraky.studentexchange.service.SkillService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserRepository userRepository;  // نحتاجه للبحث عن المستخدم

    @Override
    public SkillResponse addSkill(Long userId, SkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود بالـ ID: " + userId));

        Skill.SkillType skillType = Skill.SkillType.valueOf(request.getSkillType().toUpperCase());
        Skill.SkillLevel skillLevel = Skill.SkillLevel.valueOf(request.getSkillLevel().toUpperCase());

        // ============================================================
        // 3. إنشاء كائن Skill جديد
        // ============================================================
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        skill.setSkillType(skillType);
        skill.setSkillLevel(skillLevel);
        skill.setUser(user);  // ربط المهارة بالمستخدم

        // ============================================================
        // 4. حفظ المهارة في قاعدة البيانات
        // ============================================================
        Skill savedSkill = skillRepository.save(skill);

        // ============================================================
        // 5. تحويل Skill إلى SkillResponse وإرجاعها
        // ============================================================
        return mapToResponse(savedSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponse getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المهارة غير موجودة بالـ ID: " + id));
        return mapToResponse(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByUser(Long userId) {
        // ============================================================
        // نتأكد من وجود المستخدم أولاً
        // ============================================================
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("المستخدم غير موجود بالـ ID: " + userId);
        }

        return skillRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkills(String name) {
        return skillRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByCategory(String category) {
        return skillRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByType(String skillType) {
        // ============================================================
        // تحويل النص إلى Enum
        // ============================================================
        Skill.SkillType type = Skill.SkillType.valueOf(skillType.toUpperCase());
        return skillRepository.findBySkillType(type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        // ============================================================
        // البحث عن المهارة
        // ============================================================
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المهارة غير موجودة بالـ ID: " + id));

        // ============================================================
        // تحديث البيانات
        // ============================================================
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        skill.setSkillType(Skill.SkillType.valueOf(request.getSkillType().toUpperCase()));
        skill.setSkillLevel(Skill.SkillLevel.valueOf(request.getSkillLevel().toUpperCase()));

        Skill updatedSkill = skillRepository.save(skill);
        return mapToResponse(updatedSkill);
    }
    @Override
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new RuntimeException("المهارة غير موجودة بالـ ID: " + id);
        }
        skillRepository.deleteById(id);
    }
    @Override
    public SkillResponse updateSkillForUser(Long userId, Long skillId, SkillRequest request) {
        // ============================================================
        // 1. البحث عن المهارة
        // ============================================================
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("المهارة غير موجودة بالـ ID: " + skillId));

        // ============================================================
        // 2. التحقق من أن المهارة تنتمي للمستخدم
        // ============================================================
        if (!skill.getUser().getId().equals(userId)) {
            throw new RuntimeException("هذه المهارة لا تنتمي للمستخدم رقم: " + userId);
        }

        // ============================================================
        // 3. تحديث البيانات
        // ============================================================
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        skill.setSkillType(Skill.SkillType.valueOf(request.getSkillType().toUpperCase()));
        skill.setSkillLevel(Skill.SkillLevel.valueOf(request.getSkillLevel().toUpperCase()));

        Skill updatedSkill = skillRepository.save(skill);
        return mapToResponse(updatedSkill);
    }

    @Override
    public void deleteSkillForUser(Long userId, Long skillId) {
        // ============================================================
        // 1. البحث عن المهارة
        // ============================================================
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("المهارة غير موجودة بالـ ID: " + skillId));

        // ============================================================
        // 2. التحقق من أن المهارة تنتمي للمستخدم
        // ============================================================
        if (!skill.getUser().getId().equals(userId)) {
            throw new RuntimeException("هذه المهارة لا تنتمي للمستخدم رقم: " + userId);
        }

        skillRepository.delete(skill);
    }

    @Override
    public Map<String, Object> getUserSkillStats(Long userId) {
        // ============================================================
        // التأكد من وجود المستخدم
        // ============================================================
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("المستخدم غير موجود بالـ ID: " + userId);
        }

        // ============================================================
        // جمع الإحصائيات
        // ============================================================
        List<Skill> userSkills = skillRepository.findByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSkills", userSkills.size());
        stats.put("teachSkills", userSkills.stream()
                .filter(s -> s.getSkillType() == Skill.SkillType.TEACH).count());
        stats.put("learnSkills", userSkills.stream()
                .filter(s -> s.getSkillType() == Skill.SkillType.LEARN).count());

        // ============================================================
        // إحصائيات حسب المستوى
        // ============================================================
        Map<String, Long> byLevel = new HashMap<>();
        for (Skill.SkillLevel level : Skill.SkillLevel.values()) {
            byLevel.put(level.name(), userSkills.stream()
                    .filter(s -> s.getSkillLevel() == level).count());
        }
        stats.put("byLevel", byLevel);

        // ============================================================
        // إحصائيات حسب التصنيف
        // ============================================================
        Map<String, Long> byCategory = userSkills.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "بدون تصنيف",
                        Collectors.counting()));
        stats.put("byCategory", byCategory);

        return stats;
    }
    // ============================================================
    // دالة مساعدة: تحويل Skill إلى SkillResponse
    // ============================================================
    private SkillResponse mapToResponse(Skill skill) {
        SkillResponse response = new SkillResponse();
        response.setId(skill.getId());
        response.setName(skill.getName());
        response.setCategory(skill.getCategory());
        response.setDescription(skill.getDescription());

        // ============================================================
        // تحويل Enum إلى نص
        // skillType.name() = "TEACH" أو "LEARN"
        // ============================================================
        response.setSkillType(skill.getSkillType().name());
        response.setLevel(skill.getSkillLevel().name());

        // ============================================================
        // إضافة معلومات المستخدم (بدون البيانات الحساسة)
        // ============================================================
        response.setUserId(skill.getUser().getId());
        response.setUsername(skill.getUser().getUsername());

        response.setCreatedAt(skill.getCreatedAt());

        return response;
    }
}
