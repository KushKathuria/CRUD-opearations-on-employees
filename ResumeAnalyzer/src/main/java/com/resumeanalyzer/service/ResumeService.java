package com.resumeanalyzer.service;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final ResumeRepository resumeRepository;

    public ResumeService(S3Client s3Client,
                       @Value("${aws.s3.bucket-name}") String bucketName,
                       ResumeRepository resumeRepository) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.resumeRepository = resumeRepository;
    }

    public String uploadFile(MultipartFile file, String username) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileKey = generateFileKey(username, file.getOriginalFilename());
        Path tempFilePath = null; // Initialize to null

        try {
            tempFilePath = saveToTempLocation(file);
            uploadToS3(fileKey, tempFilePath);
            return fileKey;
        } catch (S3Exception e) {
            logger.error("Failed to upload to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload to S3: " + e.getMessage());
        } finally {
            try {
                cleanupTempFile(tempFilePath);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file {}: {}", tempFilePath, e.getMessage());
            }
        }
    }

    private String generateFileKey(String username, String originalFilename) {
        // Sanitize filename to remove spaces and potentially problematic characters
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("%s/%s_%s", 
            username, 
            UUID.randomUUID(), 
            sanitizedFilename);
    }

    private Path saveToTempLocation(MultipartFile file) throws IOException {
        // Create a unique temporary file name to avoid conflicts
        Path tempPath = Files.createTempFile(
            Paths.get(System.getProperty("java.io.tmpdir")), 
            "upload-", 
            file.getOriginalFilename()
        );
        file.transferTo(tempPath);
        return tempPath;
    }

    private void uploadToS3(String fileKey, Path filePath) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build(),
            filePath
        );
    }

    private void cleanupTempFile(Path filePath) throws IOException {
        if (filePath != null && Files.exists(filePath)) {
            Files.delete(filePath); // Use delete instead of deleteIfExists for clearer error if it fails
        }
    }

    public boolean deleteResumeById(Long id) {
        if (resumeRepository.existsById(id)) {
            resumeRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public boolean deleteResume(Long id) {
        // This method is a duplicate of deleteResumeById, keeping it for now but consider consolidating
        return deleteResumeById(id);
    }

    public Resume saveResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    public List<Resume> getResumesByUsername(String username) {
        return resumeRepository.findByUsername(username);
    }

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }
    public List<Resume> searchResumes(String query) {
        // The current search only checks name and skills. Consider adding other fields if needed.
        return resumeRepository.findByNameContainingIgnoreCaseOrSkillsContainingIgnoreCase(query, query);
    }
}
