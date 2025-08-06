package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ResumeDTO;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserServiceImpl implements ResumeParserService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeParserServiceImpl.class);
    
    private final Tika tika = new Tika();
    private static final List<String> TECHNICAL_SKILLS = List.of(
        "Java", "Python", "SQL", "Spring Boot", "JavaScript", 
        "React", "AWS", "Docker", "Kubernetes", "Machine Learning",
        "C++", "HTML", "CSS", "RESTful APIs", "Git", "Maven", "JPA", "Hibernate",
        "Microservices", "Spring Cloud", "Kafka", "MongoDB", "PostgreSQL", "Redis"
    ); // Expanded list for better skill detection

    @Override
    public ResumeDTO parseResume(MultipartFile file) throws IOException {
        try {
            String text = extractText(file);
            ResumeDTO dto = new ResumeDTO();
            
            extractContactInfo(text, dto);
            extractSkills(text, dto);
            extractEducation(text, dto);
            extractProjects(text, dto);
            extractSummary(text, dto);
            
            return dto;
        } catch (Exception e) {
            logger.error("Failed to parse resume", e);
            throw new IOException("Resume parsing failed: " + e.getMessage());
        }
    }

    private String extractText(MultipartFile file) throws IOException {
        try {
            String content = tika.parseToString(file.getInputStream());
            if (content == null || content.trim().isEmpty()) {
                throw new IOException("Empty or unreadable file content");
            }
            return content;
        } catch (Exception e) {
            throw new IOException("Text extraction failed: " + e.getMessage());
        }
    }

    private void extractContactInfo(String text, ResumeDTO dto) {
        // Email extraction
        Matcher emailMatcher = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b"
        ).matcher(text);
        if (emailMatcher.find()) {
            dto.setEmail(emailMatcher.group().trim());
        }

        // Phone extraction
        Matcher phoneMatcher = Pattern.compile(
            "(\\+\\d{1,3}[-.\\s]?)?(\\d{3}[-.\\s]?){2}\\d{4}" // More flexible phone number regex
        ).matcher(text);
        if (phoneMatcher.find()) {
            dto.setPhone(phoneMatcher.group().trim());
        }

        // Improved name extraction: Look for common patterns at the beginning of the document
        // This is still heuristic and might need more advanced NLP for perfect accuracy
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            // Pattern 1: Looks for capitalized words, potentially two or three words long, at the start of the line
            // and not containing numbers or special characters (except hyphen for names like "Mary-Ann")
            Pattern namePattern1 = Pattern.compile("^[A-Z][a-z]+(?:[- ][A-Z][a-z]+){0,2}$");
            Matcher nameMatcher1 = namePattern1.matcher(trimmedLine);
            if (nameMatcher1.matches() && trimmedLine.length() > 3 && trimmedLine.split(" ").length <= 3) {
                dto.setName(trimmedLine);
                return;
            }

            // Pattern 2: If the line contains "Name:"
            if (trimmedLine.toLowerCase().startsWith("name:")) {
                dto.setName(trimmedLine.substring(5).trim());
                return;
            }
            // Stop after checking the first few non-empty lines to avoid picking up section headers
            if (lines.length > 5 && trimmedLine.length() > 30) break; // Heuristic to stop if it's likely not a name
        }

        // Fallback if no specific name pattern is found
        if (dto.getName() == null || dto.getName().isEmpty()) {
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    dto.setName(line.trim());
                    break; // Take the first non-empty line as a last resort
                }
            }
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            dto.setName("Unknown");
        }
    }

    private void extractSkills(String text, ResumeDTO dto) {
        List<String> skills = new ArrayList<>();
        String lowerCaseText = text.toLowerCase();
        for (String skill : TECHNICAL_SKILLS) {
            if (lowerCaseText.contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        dto.setSkills(skills);
    }

    private void extractEducation(String text, ResumeDTO dto) {
        List<String> education = new ArrayList<>();
        // Improved regex to capture education section more broadly
        Pattern eduPattern = Pattern.compile(
            "(?i)(education|academic background|qualifications?|degrees?|certifications?)\\s*\\n(.*?)(?=\\n\\s*(?:work|experience|projects|skills|summary|achievements|publications|$))",
            Pattern.DOTALL
        );
        
        Matcher matcher = eduPattern.matcher(text);
        if (matcher.find()) {
            String educationSection = matcher.group(2);
            String[] eduLines = educationSection.split("\\r?\\n");
            for (String line : eduLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && 
                    trimmedLine.matches(".*\\b(university|college|institute|school|degree|bachelor|master|phd|diploma|graduated|attended)\\b.*")) {
                    education.add(trimmedLine);
                }
            }
        }
        dto.setEducation(education);
    }

    private void extractProjects(String text, ResumeDTO dto) {
        List<String> projects = new ArrayList<>();
        // Improved regex to capture projects section more broadly
        Pattern projectPattern = Pattern.compile(
            "(?i)(projects?|portfolio|key projects|personal projects)\\s*\\n(.*?)(?=\\n\\s*(?:skills|education|experience|summary|achievements|publications|$))",
            Pattern.DOTALL
        );
        
        Matcher matcher = projectPattern.matcher(text);
        if (matcher.find()) {
            String projectSection = matcher.group(2);
            // Split by lines that look like new project titles or significant breaks
            String[] projectEntries = projectSection.split("\\n(?=[A-Z][a-zA-Z0-9\\s,.-]*\\n)"); // Heuristic: new line followed by capitalized word
            for (String entry : projectEntries) {
                String trimmedEntry = entry.trim();
                if (!trimmedEntry.isEmpty()) {
                    projects.add(trimmedEntry);
                }
            }
        }
        dto.setProjects(projects);
    }

    private void extractSummary(String text, ResumeDTO dto) {
        Pattern summaryPattern = Pattern.compile(
            "(?i)(summary|about me|profile|objective|career objective)\\s*\\n(.*?)(?=\\n\\s*(?:skills|education|experience|projects|achievements|publications|$))",
            Pattern.DOTALL
        );
        
        Matcher matcher = summaryPattern.matcher(text);
        if (matcher.find()) {
            dto.setSummary(matcher.group(2).trim());
        }
    }
}
