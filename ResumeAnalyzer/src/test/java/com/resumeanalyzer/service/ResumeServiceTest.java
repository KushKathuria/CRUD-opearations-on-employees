package com.resumeanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import com.resumeanalyzer.repository.ResumeRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.nio.file.Path;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ResumeServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resumeService = new ResumeService(s3Client, "test-bucket", resumeRepository);
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "Dummy Content".getBytes()
        );

        // Act
        String uploadedKey = resumeService.uploadFile(file, "testuser");

        // Assert
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(Path.class));
        assertNotNull(uploadedKey);
        assertTrue(uploadedKey.startsWith("testuser/"));
        assertTrue(uploadedKey.contains("resume.pdf"));
    }

    @Test
    void testUploadFile_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            resumeService.uploadFile(emptyFile, "testuser");
        });
    }
}