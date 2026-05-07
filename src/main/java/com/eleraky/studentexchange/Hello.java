package com.eleraky.studentexchange;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // هذا Controller REST // كل المسارات تبدأ بـ /api/users
public class Hello {
    @GetMapping("/")
    public String hello(){
        return "Hello World!";
    }
}
