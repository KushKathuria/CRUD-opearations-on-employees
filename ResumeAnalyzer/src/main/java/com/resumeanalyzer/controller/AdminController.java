package com.resumeanalyzer.controller;

import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.service.ResumeService;
import com.resumeanalyzer.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;
    private final UserService userService;

    public AdminController(ResumeService resumeService, UserRepository userRepository, UserService userService) {
        this.resumeService = resumeService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/resumes")
    public ResponseEntity<?> getAllResumes(Authentication authentication) {
        try {
            List<Resume> resumes = resumeService.getAllResumes();
            if (resumes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No resumes found");
            }
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch resumes: " + e.getMessage());
        }
    }

    @DeleteMapping("/resume/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id, Authentication authentication) {
        try {
            boolean deleted = resumeService.deleteResume(id);
            if (deleted) {
                return ResponseEntity.ok("Resume deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting resume: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No users found");
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch users: " + e.getMessage());
        }
    }

    @PostMapping("/user/{username}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String username,
            @RequestParam String role,
            Authentication authentication) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole(role);
                userRepository.save(user);
                return ResponseEntity.ok("User role updated to: " + role);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user role: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username, Authentication authentication) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            userService.deleteUserByUsername(username);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }
}

