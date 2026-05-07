package com.eleraky.studentexchange.service.impl;


// ============================================================
// نستورد كل المكونات المطلوبة
// ============================================================
import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.UpdateUserRequest;
import com.eleraky.studentexchange.dto.response.UserResponse;
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.UserRepository;
import com.eleraky.studentexchange.service.UserService;

// ============================================================
// @Service: تخبر Spring أن هذه الفئة هي Service
// Spring سينشئ منها Bean ويديرها تلقائياً
// ============================================================
import org.springframework.stereotype.Service;

// ============================================================
// @Autowired: حقن التبعيات (Dependency Injection)
// بدلاً من إنشاء كائن UserRepository يدوياً، Spring يوفره لنا
// ============================================================
import org.springframework.beans.factory.annotation.Autowired;

// ============================================================
// @Transactional: إدارة المعاملات (Transactions)
// إذا فشل أي جزء من العملية، يتم التراجع عن كل التغييرات
// ============================================================
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service  // نعلم Spring أن هذه Service
@Transactional  // كل الدوال في هذه الفئة ستكون Transactional
public class UserServiceImpl implements UserService{

    // ============================================================
    // @Autowired: Spring يحقن (inject) كائن UserRepository هنا
    // لماذا لا نستخدم new UserRepository()؟
    // لأن UserRepository هو Interface وليس له Implementation مباشر
    // Spring ينشئ Proxy في وقت التشغيل
    // ============================================================

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest){

        // ============================================================
        // التحقق من عدم وجود username مكرر
        // ============================================================
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new RuntimeException("Username is Already Exist" + createUserRequest.getUsername());
        }
        // =====================================9=======================
        // التحقق من عدم وجود email مكرر
        // =================
        if(userRepository.existsByEmail(createUserRequest.getEmail())){
            throw new RuntimeException("Email is Already Exist" + createUserRequest.getEmail());
        }

        // ============================================================
        // إنشاء كائن User جديد
        // ننقل البيانات من CreateUserRequest إلى User
        // ============================================================

        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setFullName(createUserRequest.getFullName());
        user.setBio(createUserRequest.getBio());
        user.setPassword(createUserRequest.getPassword());

        // ============================================================
        // حفظ المستخدم في قاعدة البيانات
        // save() ترجع المستخدم بعد الحفظ (مع ID الجديد)
        // ============================================================
        User savedUser = userRepository.save(user);

        // ============================================================
        // تحويل User إلى UserResponse (بدون كلمة المرور)
        // ============================================================
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)  // للقراءة فقط - أداء أفضل
    public List<UserResponse> getAllUsers() {
        // ============================================================
        // نجلب كل المستخدمين من قاعدة البيانات
        // ============================================================
        List<User> users = userRepository.findAll();

        return users.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {

        if (!userRepository.existsById(id)){
            throw new RuntimeException("User Not Found" + id);
        }

        return userRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)){
            throw new RuntimeException("User Not Found" + username);
        }
        return userRepository.findByUsername(username).map(this::mapToResponse);
    }

    @Override
    public Optional<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getBio() != null) user.setBio(request.getBio());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) user.setPassword(request.getPassword());
            return mapToResponse(userRepository.save(user));
        });
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)){
            throw new RuntimeException("User Not Found" + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ============================================================
    // دالة مساعدة: تحويل User إلى UserResponse
    // ============================================================
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        // ⚠️ لا نرسل كلمة المرور أبداً في الاستجابة!
        response.setFullName(user.getFullName());
        response.setBio(user.getBio());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}

