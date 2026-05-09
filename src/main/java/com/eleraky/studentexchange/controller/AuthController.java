package com.eleraky.studentexchange.controller;

import com.eleraky.studentexchange.dto.request.CreateUserRequest;
import com.eleraky.studentexchange.dto.request.LoginRequest;
import com.eleraky.studentexchange.dto.response.JwtResponse;
import com.eleraky.studentexchange.dto.response.UserResponse;
import com.eleraky.studentexchange.model.User;
import com.eleraky.studentexchange.security.JwtTokenProvider;
import com.eleraky.studentexchange.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticateUser(request.getUsernameOrEmail(), request.getPassword());
        String jwt = tokenProvider.generateToken(user);
        JwtResponse response = new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getFullName());
        return ResponseEntity.ok(response);
    }
}
