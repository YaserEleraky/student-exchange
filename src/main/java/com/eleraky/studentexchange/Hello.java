package com.eleraky.studentexchange;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {

    @GetMapping("/")
    public String hello(){
        return "Hello This My Student Exchange App!";
    }

    @GetMapping("/api/admin/hello")
    public String adminHello(){
        return "Hello This My admin!";
    }

    @GetMapping("/api/superadmin/hello")
    public String superAdminHello(){
        return "Hello This My superadmin!";
    }
}
