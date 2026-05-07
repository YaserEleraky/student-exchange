package com.eleraky.studentexchange.controller;

import com.eleraky.studentexchange.dto.request.SkillRequest;
import com.eleraky.studentexchange.dto.response.SkillResponse;
import com.eleraky.studentexchange.service.SkillService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    // ============================================================
    // POST /api/skills/user/{userId}
    // إضافة مهارة جديدة لمستخدم
    // ============================================================
    @PostMapping("/user/{userId}")
    public ResponseEntity<SkillResponse> addSkill(
            @PathVariable Long userId,
            @Valid @RequestBody SkillRequest request) {

        SkillResponse response = skillService.addSkill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // GET /api/skills
    // الحصول على جميع المهارات
    // ============================================================
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    // ============================================================
    // GET /api/skills/{id}
    // الحصول على مهارة بالـ ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    // ============================================================
    // GET /api/skills/user/{userId}
    // الحصول على كل مهارات مستخدم معين
    // ============================================================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SkillResponse>> getSkillsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(skillService.getSkillsByUser(userId));
    }

    // ============================================================
    // GET /api/skills/search?name=java
    // البحث عن مهارات بالاسم
    // ============================================================
    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(
            @RequestParam String name) {
        return ResponseEntity.ok(skillService.searchSkills(name));
    }

    // ============================================================
    // GET /api/skills/category/{category}
    // الحصول على مهارات حسب التصنيف
    // ============================================================
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SkillResponse>> getSkillsByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(skillService.getSkillsByCategory(category));
    }

    // ============================================================
    // GET /api/skills/type/{skillType}
    // الحصول على مهارات حسب النوع (TEACH أو LEARN)
    // ============================================================
    @GetMapping("/type/{skillType}")
    public ResponseEntity<List<SkillResponse>> getSkillsByType(
            @PathVariable String skillType) {
        return ResponseEntity.ok(skillService.getSkillsByType(skillType));
    }

    // ============================================================
    // PUT /api/skills/{id}
    // تحديث مهارة
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillService.updateSkill(id, request));
    }

    // ============================================================
    // DELETE /api/skills/{id}
    // حذف مهارة
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
    // ============================================================
// PUT /api/skills/user/{userId}/skill/{skillId}
// تحديث مهارة معينة لمستخدم معين
// ============================================================
    @PutMapping("/user/{userId}/skill/{skillId}")
    public ResponseEntity<SkillResponse> updateSkillForUser(
            @PathVariable Long userId,
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest request) {

        SkillResponse response = skillService.updateSkillForUser(userId, skillId, request);
        return ResponseEntity.ok(response);
    }

    // ============================================================
// DELETE /api/skills/user/{userId}/skill/{skillId}
// حذف مهارة معينة لمستخدم معين
// ============================================================
    @DeleteMapping("/user/{userId}/skill/{skillId}")
    public ResponseEntity<Void> deleteSkillForUser(
            @PathVariable Long userId,
            @PathVariable Long skillId) {

        skillService.deleteSkillForUser(userId, skillId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
// GET /api/skills/user/{userId}/stats
// إحصائيات مهارات المستخدم
// ============================================================
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserSkillStats(
            @PathVariable Long userId) {
        return ResponseEntity.ok(skillService.getUserSkillStats(userId));
    }
}