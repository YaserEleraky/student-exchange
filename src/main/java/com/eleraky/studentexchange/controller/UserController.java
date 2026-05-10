package com.eleraky.studentexchange.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.UpdateUserRequest;
import com.eleraky.studentexchange.dto.response.UserResponse;
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.UserRepository;
import com.eleraky.studentexchange.service.UserService;
import com.eleraky.studentexchange.service.impl.FileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    // POST /api/users - إنشاء مستخدم مع صورة
    // ============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(
            @ModelAttribute @Valid CreateUserRequest request, // تغيير RequestPart إلى ModelAttribute
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        try {
            UserResponse response = userService.createUser(request, avatar);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // ============================================================
    // GET /api/users - كل المستخدمين
    // ============================================================
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ============================================================
    // GET /api/users/{id} - مستخدم بالـ ID
    // ============================================================
    @GetMapping("/{id:\\d+}") // ← أرقام فقط
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // GET /api/users/username/{username} - مستخدم بالاسم
    // ============================================================
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // PUT /api/users/{id} - تحديث مستخدم
    // ============================================================
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // PUT /api/users/{id}/profile - تحديث مستخدم مع صورة
    // ============================================================
    @PutMapping(value = "/profile/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserWithAvatar(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateUserRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return userService.updateUserWithAvatar(id, request, avatar)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // DELETE /api/users/{id} - حذف مستخدم
    // ============================================================
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // POST /api/users/{id}/avatar - رفع صورة
    // ============================================================
    @PostMapping("/avatar/{id:\\d+}")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // حذف القديم
            if (user.getAvatarPath() != null) {
                fileStorageService.deleteAvatar(user.getAvatarPath());
            }

            // حفظ الجديد
            String avatarPath = fileStorageService.saveAvatar(file, id);
            user.setAvatarPath(avatarPath);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Avatar uploaded successfully",
                    "path", avatarPath));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/users/avatar/{id} - عرض الصورة
    // لاحظ: المسار مختلف! avatar قبل id
    // ============================================================
    @GetMapping("/avatar/{id:\\d+}")
    public ResponseEntity<?> getAvatar(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) return ResponseEntity.notFound().build();

        // Try local file first
        if (user.getAvatarPath() != null) {
            try {
                Path path = Paths.get(user.getAvatarPath().substring(1));
                if (Files.exists(path)) {
                    byte[] imageBytes = Files.readAllBytes(path);
                    String contentType = Files.probeContentType(path);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                            .body(imageBytes);
                }
            } catch (IOException ignored) {}
        }

        // Fallback: redirect to OAuth2 provider image URL (Google / GitHub)
        if (user.getImageUrl() != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, user.getImageUrl())
                    .build();
        }

        return ResponseEntity.notFound().build();
    }
}