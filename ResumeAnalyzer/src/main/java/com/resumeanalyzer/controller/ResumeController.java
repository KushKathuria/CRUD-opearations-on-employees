package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.ResumeDTO;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeParserService;
import com.resumeanalyzer.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);
    
    private final ResumeService resumeService;
    private final ResumeParserService resumeParserService;

    public ResumeController(ResumeService resumeService, 
                          ResumeParserService resumeParserService) {
        this.resumeService = resumeService;
        this.resumeParserService = resumeParserService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }

        try {
            String username = authentication.getName();
            logger.info("Processing resume upload for user: {}", username);

            // 1. Upload to S3
            String s3Key = resumeService.uploadFile(file, username);
            logger.debug("File uploaded to S3 with key: {}", s3Key);

            // 2. Parse resume
            ResumeDTO parsedData = resumeParserService.parseResume(file);
            logger.debug("Resume parsed successfully for: {}", parsedData.getName());

            // 3. Save to database
            Resume resume = createResumeEntity(parsedData, username);
            Resume savedResume = resumeService.saveResume(resume);
            logger.info("Resume saved with ID: {}", savedResume.getId());

            return ResponseEntity.ok(
                new UploadResponse(
                    s3Key,
                    savedResume.getId(),
                    "Resume processed successfully"
                )
            );
            
        } catch (IOException e) {
            logger.error("File upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during resume processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getUserResumes(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.debug("Fetching resumes for user: {}", username);
            return ResponseEntity.ok(
                resumeService.getResumesByUsername(username)
            );
        } catch (Exception e) {
            logger.error("Failed to fetch user resumes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Failed to fetch resumes");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllResumes(Authentication authentication) {
        try {
            // Check if user has admin role
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only admins can view all resumes.");
            }
            
            logger.debug("Fetching all resumes");
            return ResponseEntity.ok(resumeService.getAllResumes());
        } catch (Exception e) {
            logger.error("Failed to fetch all resumes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Failed to fetch resumes");
        }
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
        try {
            ResumeDTO parsedData = resumeParserService.parseResume(file);
            return ResponseEntity.ok(parsedData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body("Failed to parse resume: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Unexpected error: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchResumes(@RequestParam("query") String query, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Resume> results = resumeService.searchResumes(query);
            
            // Filter results to only include resumes belonging to the user or all if admin
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                results = results.stream()
                    .filter(resume -> resume.getUsername().equals(username))
                    .toList();
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Search failed: " + e.getMessage());
        }
    }

    private Resume createResumeEntity(ResumeDTO dto, String username) {
        Resume resume = new Resume();
        resume.setName(dto.getName());
        resume.setEmail(dto.getEmail());
        resume.setPhone(dto.getPhone());
        resume.setSkills(dto.getSkills());
        resume.setEducation(dto.getEducation());
        resume.setProjects(dto.getProjects());
        resume.setSummary(dto.getSummary());
        resume.setUsername(username);
        return resume;
    }

    // Response DTO
    private static class UploadResponse {
        private final String s3Key;
        private final Long resumeId;
        private final String message;

        public UploadResponse(String s3Key, Long resumeId, String message) {
            this.s3Key = s3Key;
            this.resumeId = resumeId;
            this.message = message;
        }

        // Getters
        public String getS3Key() { return s3Key; }
        public Long getResumeId() { return resumeId; }
        public String getMessage() { return message; }
    }
}

