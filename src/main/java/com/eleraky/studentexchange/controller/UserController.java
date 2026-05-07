package com.eleraky.studentexchange.controller;

// ============================================================
// @RestController: تخبر Spring أن هذه الفئة هي Controller
// تجمع بين @Controller و @ResponseBody
// كل دالة سترجع JSON تلقائياً
// ============================================================
import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.UpdateUserRequest;
import com.eleraky.studentexchange.dto.response.UserResponse;
import com.eleraky.studentexchange.service.UserService;

// ============================================================
// @Valid: تفعيل التحقق من صحة البيانات (Validation)
// إذا فشل التحقق، Spring يرجع خطأ 400 تلقائياً
// ============================================================
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // هذا Controller REST
@RequestMapping("/api/users")  // كل المسارات تبدأ بـ /api/users
public class UserController {

    // ============================================================
    // حقن UserService
    // ============================================================
    @Autowired
    private UserService userService;

    // ============================================================
    // POST /api/users
    // إنشاء مستخدم جديد
    // @Valid: يتحقق من صحة البيانات قبل تنفيذ الدالة
    // @RequestBody: يستخرج البيانات من جسم الطلب (Request Body)
    // ===========================================================

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest){
        // ============================================================
        // ResponseEntity: نتحكم في HTTP Status Code
        // HttpStatus.CREATED = 201 (تم الإنشاء بنجاح)
        // ============================================================
        UserResponse response = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // ============================================================
        // GET /api/users
        // الحصول على جميع المستخدمين
        // ============================================================
        @GetMapping
        public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
        }

        // ============================================================
        // GET /api/users/{id}
        // الحصول على مستخدم بالـ ID
        // @PathVariable: يستخرج {id} من المسار
        // ============================================================
        @GetMapping("/{id:[0-9]+}")
        public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
            return userService.getUserById(id)
                    .map(ResponseEntity::ok)  // إذا وُجد، نرجع 200 OK
                    .orElse(ResponseEntity.notFound().build()); // إذا لم يُوجد، نرجع 404
        }
        // ============================================================
        // GET /api/users/username/{username}
        // البحث عن مستخدم باسم المستخدم
        // ============================================================
        @GetMapping("/username/{username}")
        public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username){
            return userService.getUserByUsername(username).
                    map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }

        // ============================================================
        // PUT /api/users/{id}
        // تحديث مستخدم
        // ============================================================
        @PutMapping("/{id}")
        public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest){
            return userService.updateUser(id, updateUserRequest).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }
        // ============================================================
        // DELETE /api/users/{id}
        // حذف مستخدم
        // ============================================================
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        }
    }
