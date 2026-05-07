package com.eleraky.studentexchange.controller;

import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // جلب جميع المستخدمين
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // إضافة مستخدم جديد
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
    
    // جلب مستخدم معين بالمعرف
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
