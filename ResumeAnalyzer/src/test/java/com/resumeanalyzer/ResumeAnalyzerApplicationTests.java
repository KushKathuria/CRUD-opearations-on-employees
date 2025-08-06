package com.resumeanalyzer;  // Make sure this matches your actual package

import com.resumeanalyzer.service.S3Service;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
class ResumeAnalyzerApplicationTests {

    @Test
    void contextLoads() {
        // Test that context loads successfully
    }

    @Configuration
    static class TestConfig {
        @Bean
        public S3Client s3Client() {
            return mock(S3Client.class);
        }

        @Bean 
        public S3Service s3Service() {
            return new S3Service(s3Client());
        }
    }
}