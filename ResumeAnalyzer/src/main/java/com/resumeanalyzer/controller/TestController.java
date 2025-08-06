package com.resumeanalyzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public OK";
    }

    @GetMapping("/protected")
    public ResponseEntity<String> testProtected(Authentication authentication) {
        return ResponseEntity.ok("You are authorized!");
    }

    @GetMapping("/auth")
    public ResponseEntity<?> authDebug(Authentication authentication) {
        return ResponseEntity.ok(authentication);
    }
}

