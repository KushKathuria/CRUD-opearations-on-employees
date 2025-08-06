package com.resumeanalyzer.controller;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumeanalyzer.service.ResumeService;
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final ResumeService resumeService;

    public UserController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/my-resumes")
    public ResponseEntity<?> getMyResumes(Authentication authentication) {
        try {
            String username = authentication.getName();
            return ResponseEntity.ok(resumeService.getResumesByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch resumes: " + e.getMessage());
        }
    }
}

