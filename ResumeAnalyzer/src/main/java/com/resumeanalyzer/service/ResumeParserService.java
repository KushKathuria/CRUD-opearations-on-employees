package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ResumeDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ResumeParserService {
    ResumeDTO parseResume(MultipartFile file) throws IOException;
}